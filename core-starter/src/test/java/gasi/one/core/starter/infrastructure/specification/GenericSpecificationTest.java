package gasi.one.core.starter.infrastructure.specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import gasi.one.core.api.common.exception.BusinessException;
import gasi.one.core.api.common.exception.ErrorDetail;
import gasi.one.core.api.common.query.AndFilter;
import gasi.one.core.api.common.query.SimpleFilter;
import gasi.one.core.api.common.query.SimpleFilter.FilterOperator;
import gasi.one.core.starter.infrastructure.entity.BaseEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

class GenericSpecificationTest {

    @Test
    void rejectsEmptyFilterGroups() {
        GenericSpecification<TestEntity> specification = new GenericSpecification<>(
                AndFilter.builder().build());

        assertThatThrownBy(() -> specification.toPredicate(root(Long.class), null, mock(CriteriaBuilder.class)))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorDetails())
                        .extracting(ErrorDetail::getCode)
                        .containsExactly("FILTER_GROUP_EMPTY"));
    }

    @Test
    void rejectsNonComparableValueTypeBeforeJpaExecution() {
        GenericSpecification<TestEntity> specification = new GenericSpecification<>(
                SimpleFilter.builder()
                        .field("id")
                        .operator(FilterOperator.GREATER_THAN)
                        .value("not-a-number")
                        .build());

        assertThatThrownBy(() -> specification.toPredicate(root(Long.class), null, mock(CriteriaBuilder.class)))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorDetails())
                        .extracting(ErrorDetail::getCode)
                        .containsExactly("FILTER_VALUE_TYPE_INVALID"));
    }

    @Test
    void rejectsInValueThatIsNotACollection() {
        GenericSpecification<TestEntity> specification = new GenericSpecification<>(
                SimpleFilter.builder()
                        .field("id")
                        .operator(FilterOperator.IN)
                        .value(1L)
                        .build());

        assertThatThrownBy(() -> specification.toPredicate(root(Long.class), null, mock(CriteriaBuilder.class)))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorDetails())
                        .extracting(ErrorDetail::getCode)
                        .containsExactly("FILTER_VALUE_TYPE_INVALID"));
    }

    @SuppressWarnings("unchecked")
    private Root<TestEntity> root(Class<?> pathType) {
        Root<TestEntity> root = mock(Root.class);
        Path<Object> path = mock(Path.class);
        doReturn(TestEntity.class).when(root).getJavaType();
        when(root.get("id")).thenReturn(path);
        doReturn(pathType).when(path).getJavaType();
        return root;
    }

    static class TestEntity extends BaseEntity {
    }
}
