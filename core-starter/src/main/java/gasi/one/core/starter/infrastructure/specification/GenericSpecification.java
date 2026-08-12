package gasi.one.core.starter.infrastructure.specification;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import gasi.one.core.api.common.exception.BusinessException;
import gasi.one.core.api.common.exception.ErrorDetail;
import gasi.one.core.api.common.query.AndFilter;
import gasi.one.core.api.common.query.GenericFilter;
import gasi.one.core.api.common.query.OrFilter;
import gasi.one.core.api.common.query.SimpleFilter;
import gasi.one.core.api.common.query.SimpleFilter.FilterOperator;
import gasi.one.core.starter.infrastructure.filter.FilterableFieldResolver;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Converts a {@link GenericFilter} tree into a JPA {@link Specification}.
 *
 * <p>
 * Simple filter fields are resolved through {@link FilterableFieldResolver}
 * against entity field names.
 * </p>
 *
 * @param <T> entity type
 * @since 1.0.0
 */
public class GenericSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 1L;

    /** Filter expression converted by this specification. */
    private final GenericFilter filter;

    /**
     * Creates a specification for a filter expression.
     *
     * @param filter filter expression
     */
    public GenericSpecification(GenericFilter filter) {
        this.filter = filter;
    }

    /**
     * Creates a JPA specification for a filter expression.
     *
     * @param filter filter expression, or {@code null} to match all records
     * @param <T>    entity type
     * @return JPA specification
     */
    public static <T> Specification<T> from(GenericFilter filter) {
        if (filter == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return new GenericSpecification<>(filter);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return buildPredicate(filter, root, cb);
    }

    private Predicate buildPredicate(GenericFilter genericFilter, Root<T> root, CriteriaBuilder cb) {
        if (genericFilter == null) {
            throw filterError("FILTER_REQUIRED", "filter", "error.filter.required");
        }
        if (genericFilter instanceof SimpleFilter simpleFilter) {
            return buildSimplePredicate(simpleFilter, root, cb);
        } else if (genericFilter instanceof AndFilter andFilter) {
            List<GenericFilter> filters = requireChildFilters(andFilter.getFilters());
            Predicate[] predicates = filters.stream()
                    .map(f -> buildPredicate(f, root, cb))
                    .toArray(Predicate[]::new);
            return cb.and(predicates);
        } else if (genericFilter instanceof OrFilter orFilter) {
            List<GenericFilter> filters = requireChildFilters(orFilter.getFilters());
            Predicate[] predicates = filters.stream()
                    .map(f -> buildPredicate(f, root, cb))
                    .toArray(Predicate[]::new);
            return cb.or(predicates);
        }
        throw filterError("FILTER_TYPE_UNSUPPORTED", "filter", "error.filter.typeUnsupported");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Predicate buildSimplePredicate(SimpleFilter sf, Root<T> root, CriteriaBuilder cb) {
        if (sf.getField() == null || sf.getField().isBlank()) {
            throw filterError("FILTER_FIELD_REQUIRED", "field", "error.filter.fieldRequired");
        }
        if (sf.getOperator() == null) {
            throw filterError("FILTER_OPERATOR_REQUIRED", "operator", "error.filter.operatorRequired");
        }

        String field = FilterableFieldResolver.resolve(root.getJavaType(), sf.getField());
        Path<?> path = resolvePath(root, field);
        Object value = coerceValue(path, sf.getValue(), sf.getField());
        validateOperatorValue(sf.getOperator(), path, value, sf.getField());

        return switch (sf.getOperator()) {
            case EQUALS -> cb.equal(path, value);
            case NOT_EQUALS -> cb.notEqual(path, value);
            case GREATER_THAN -> cb.greaterThan((Path<? extends Comparable>) path, (Comparable) value);
            case GREATER_THAN_OR_EQUALS -> cb.greaterThanOrEqualTo(
                    (Path<? extends Comparable>) path, (Comparable) value);
            case LESS_THAN -> cb.lessThan((Path<? extends Comparable>) path, (Comparable) value);
            case LESS_THAN_OR_EQUALS -> cb.lessThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) value);
            case LIKE -> cb.like(path.as(String.class), "%" + value.toString() + "%");
            case IN -> path.in((Collection<?>) value);
            case IS_NULL -> cb.isNull(path);
            case IS_NOT_NULL -> cb.isNotNull(path);
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object coerceValue(Path<?> path, Object value, String field) {
        if (value == null) {
            return null;
        }

        if (value instanceof Collection<?> values) {
            return values.stream()
                    .map(item -> coerceSingleValue(path, item, field))
                    .toList();
        }

        return coerceSingleValue(path, value, field);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object coerceSingleValue(Path<?> path, Object value, String field) {
        if (value == null) {
            return null;
        }

        Class<?> javaType = path.getJavaType();
        if (!javaType.isEnum()) {
            if (Number.class.isAssignableFrom(wrapperType(javaType)) && value instanceof Number number) {
                return coerceNumber(wrapperType(javaType), number, field);
            }
            return value;
        }

        if (javaType.isInstance(value)) {
            return value;
        }

        if (value instanceof String text) {
            try {
                return Enum.valueOf((Class<? extends Enum>) javaType.asSubclass(Enum.class), text);
            } catch (IllegalArgumentException ex) {
                throw filterError("FILTER_VALUE_INVALID", field, "error.filter.valueInvalid");
            }
        }

        if (value instanceof Number number) {
            Object[] constants = javaType.getEnumConstants();
            int ordinal = number.intValue();
            if (ordinal >= 0 && ordinal < constants.length) {
                return constants[ordinal];
            }
        }

        return value;
    }

    private List<GenericFilter> requireChildFilters(List<GenericFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            throw filterError("FILTER_GROUP_EMPTY", "filters", "error.filter.groupEmpty");
        }
        if (filters.stream().anyMatch(filter -> filter == null)) {
            throw filterError("FILTER_GROUP_INVALID", "filters", "error.filter.groupInvalid");
        }
        return filters;
    }

    private void validateOperatorValue(FilterOperator operator, Path<?> path, Object value, String field) {
        switch (operator) {
            case IS_NULL, IS_NOT_NULL -> {
                return;
            }
            case IN -> {
                if (!(value instanceof Collection<?> values)) {
                    throw filterError("FILTER_VALUE_TYPE_INVALID", field, "error.filter.inValueMustBeCollection");
                }
                values.forEach(item -> ensureCompatibleValue(path, item, field));
            }
            case LIKE -> {
                requireValue(value, field);
                if (!CharSequence.class.isAssignableFrom(wrapperType(path.getJavaType()))
                        || !(value instanceof CharSequence)) {
                    throw filterError("FILTER_VALUE_TYPE_INVALID", field, "error.filter.likeValueMustBeText");
                }
            }
            case GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS -> {
                requireValue(value, field);
                if (!Comparable.class.isAssignableFrom(wrapperType(path.getJavaType()))
                        || !(value instanceof Comparable<?>)) {
                    throw filterError("FILTER_VALUE_TYPE_INVALID", field, "error.filter.valueMustBeComparable");
                }
                ensureCompatibleValue(path, value, field);
            }
            case EQUALS, NOT_EQUALS -> ensureCompatibleValue(path, value, field);
        }
    }

    private void requireValue(Object value, String field) {
        if (value == null) {
            throw filterError("FILTER_VALUE_REQUIRED", field, "error.filter.valueRequired");
        }
    }

    private void ensureCompatibleValue(Path<?> path, Object value, String field) {
        if (value == null) {
            return;
        }
        Class<?> javaType = wrapperType(path.getJavaType());
        if (!javaType.isInstance(value)) {
            throw filterError("FILTER_VALUE_TYPE_INVALID", field, "error.filter.valueTypeInvalid");
        }
    }

    private Object coerceNumber(Class<?> javaType, Number number, String field) {
        try {
            if (Integer.class.equals(javaType)) {
                return number.intValue();
            }
            if (Long.class.equals(javaType)) {
                return number.longValue();
            }
            if (Double.class.equals(javaType)) {
                return number.doubleValue();
            }
            if (Float.class.equals(javaType)) {
                return number.floatValue();
            }
            if (Short.class.equals(javaType)) {
                return number.shortValue();
            }
            if (Byte.class.equals(javaType)) {
                return number.byteValue();
            }
            if (BigInteger.class.equals(javaType)) {
                return BigInteger.valueOf(number.longValue());
            }
            if (BigDecimal.class.equals(javaType)) {
                return new BigDecimal(number.toString());
            }
            return number;
        } catch (NumberFormatException ex) {
            throw filterError("FILTER_VALUE_INVALID", field, "error.filter.valueInvalid");
        }
    }

    private Class<?> wrapperType(Class<?> javaType) {
        if (!javaType.isPrimitive()) {
            return javaType;
        }
        if (int.class.equals(javaType)) {
            return Integer.class;
        }
        if (long.class.equals(javaType)) {
            return Long.class;
        }
        if (double.class.equals(javaType)) {
            return Double.class;
        }
        if (float.class.equals(javaType)) {
            return Float.class;
        }
        if (short.class.equals(javaType)) {
            return Short.class;
        }
        if (byte.class.equals(javaType)) {
            return Byte.class;
        }
        if (boolean.class.equals(javaType)) {
            return Boolean.class;
        }
        if (char.class.equals(javaType)) {
            return Character.class;
        }
        return javaType;
    }

    private BusinessException filterError(String code, String field, String message) {
        return BusinessException.of(ErrorDetail.of(code, field, message));
    }

    private Path<?> resolvePath(Root<T> root, String field) {
        Path<?> path = root;
        for (String part : field.split("\\.")) {
            path = path.get(part);
        }
        return path;
    }
}
