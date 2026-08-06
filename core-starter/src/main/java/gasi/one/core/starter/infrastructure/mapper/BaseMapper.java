package gasi.one.core.starter.infrastructure.mapper;

import org.mapstruct.MappingTarget;

import gasi.one.core.api.resource.model.BaseModel;
import gasi.one.core.starter.application.mapper.IgnoreManagedFields;
import gasi.one.core.starter.infrastructure.entity.BaseEntity;

/**
 * Generic MapStruct contract between domain models and JPA entities.
 *
 * <p>Repository adapters use this mapper to keep persistence concerns out of
 * domain models.</p>
 *
 * @param <D> domain model type
 * @param <E> JPA entity type
 * @since 1.0.0
 */
public interface BaseMapper<D extends BaseModel, E extends BaseEntity> {

    /**
     * Converts a JPA entity into a domain model.
     *
     * @param entity JPA entity
     * @return domain model
     */
    D toDomain(E entity);

    /**
     * Converts a domain model into a JPA entity.
     *
     * @param domain domain model
     * @return JPA entity
     */
    E toEntity(D domain);

    /**
     * Applies a domain model onto an existing JPA entity.
     *
     * @param source domain source
     * @param target existing entity to mutate
     */
    @IgnoreManagedFields
    void updateEntity(D source, @MappingTarget E target);
}
