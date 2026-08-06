package gasi.one.core.starter.application.mapper;

import gasi.one.core.api.resource.model.BaseModel;

/**
 * Generic MapStruct contract for full CRUD DTO mappings.
 *
 * <p>
 * This mapper extends {@link BaseReadDtoMapper} for summary and detail
 * response conversions, then adds create and update request mappings for full
 * CRUD resources.
 * </p>
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type (for lists)
 * @param <DRS> detail response DTO type (for single entity)
 * @since 1.0.0
 */
public interface BaseDtoMapper<D extends BaseModel, CRQ, URQ, SRS, DRS>
        extends BaseReadDtoMapper<D, SRS, DRS> {

    /**
     * Converts a create request into a new domain model.
     *
     * @param createRequest create request DTO
     * @return domain model populated from the create request
     */
    @IgnoreManagedFields
    D toCreateDomain(CRQ createRequest);

    /**
     * Converts an update request into a domain model.
     *
     * @param updateRequest update request DTO
     * @return domain model populated from the update request
     */
    @IgnoreManagedFields
    D toUpdateDomain(URQ updateRequest);

    /**
     * Applies an update request onto an existing domain model.
     *
     * @param updateRequest update request DTO
     * @param domain        existing domain model to mutate
     */
    @IgnoreManagedFields
    void updateDomain(URQ updateRequest, @org.mapstruct.MappingTarget D domain);

    /**
     * Clones an existing domain model into a new domain model.
     *
     * @param source existing domain model to clone
     * @return cloned domain model
     */
    @IgnoreManagedFields
    D cloneDomain(D source);

    /**
     * Copies values from one domain model into an existing domain model.
     *
     * <p>
     * This method is commonly used by workflow implementations that need to copy
     * values between related domain records.
     * </p>
     *
     * @param source source domain model
     * @param target target domain model to mutate
     */
    @IgnoreManagedFields
    void copyDomain(D source, @org.mapstruct.MappingTarget D target);
}
