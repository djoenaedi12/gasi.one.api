package gasi.one.core.starter.application.service;

import org.springframework.transaction.annotation.Transactional;

import gasi.one.core.api.resource.dto.VersionedRequest;
import gasi.one.core.api.common.exception.EntityNotFoundException;
import gasi.one.core.api.common.id.IdCodec;
import gasi.one.core.api.resource.hook.ResourceMapperHook;
import gasi.one.core.api.resource.hook.ResourceServiceHook;
import gasi.one.core.api.resource.model.BaseModel;
import gasi.one.core.api.resource.port.inbound.BaseService;
import gasi.one.core.api.resource.port.inbound.MutationOptions;
import gasi.one.core.api.resource.port.outbound.BaseRepositoryPort;
import gasi.one.core.starter.application.hook.ResourceMapperHookRegistry;
import gasi.one.core.starter.application.hook.ResourceServiceHookRegistry;
import gasi.one.core.starter.application.mapper.BaseDtoMapper;
import gasi.one.core.starter.infrastructure.i18n.MessageUtil;

/**
 * Generic transactional implementation of {@link BaseService}.
 *
 * <p>
 * This implementation builds on {@link BaseReadServiceImpl} for query
 * operations, then adds create, update, and delete behavior for full CRUD
 * resources. Subclasses supply concrete repository and mapper implementations.
 * Generated and custom lifecycle behavior can be added through ordered
 * {@link ResourceServiceHook} beans.
 * </p>
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO
 * @param <URQ> update request DTO
 * @param <SRS> summary response DTO (for lists)
 * @param <DRS> detail response DTO (for single entity)
 * @since 1.0.0
 */
@Transactional
public abstract class BaseServiceImpl<D extends BaseModel, CRQ, URQ, SRS, DRS>
        extends BaseReadServiceImpl<D, SRS, DRS>
        implements BaseService<D, CRQ, URQ, SRS, DRS> {

    /** Mapper used for write-side DTO conversion and inherited read mappings. */
    protected final BaseDtoMapper<D, CRQ, URQ, SRS, DRS> mapper;

    /**
     * Creates a base service implementation with ordered resource and mapper hooks.
     *
     * @param repositoryPort     repository port for domain persistence
     * @param mapper             mapper between request/response DTOs and domain
     *                           models
     * @param messageUtil        localized message helper
     * @param idCodec            public ID codec
     * @param hookRegistry       registry for generated and custom service hooks
     * @param mapperHookRegistry registry for generated and custom mapper hooks
     */
    protected BaseServiceImpl(BaseRepositoryPort<D> repositoryPort,
            BaseDtoMapper<D, CRQ, URQ, SRS, DRS> mapper,
            MessageUtil messageUtil,
            IdCodec idCodec,
            ResourceServiceHookRegistry hookRegistry,
            ResourceMapperHookRegistry mapperHookRegistry) {
        super(repositoryPort, mapper, messageUtil, idCodec, hookRegistry, mapperHookRegistry);
        this.mapper = mapper;
    }

    @Override
    public DRS create(CRQ request, MutationOptions options) {
        ResourceServiceHook<D, CRQ, URQ, SRS, DRS> hook = serviceHook();
        ResourceMapperHook<D, CRQ, URQ, SRS, DRS> mapperHook = mapperHook();
        hook.beforeCreateRequest(resourceType(), request);
        D domain = mapper.toCreateDomain(request);
        mapperHook.afterToCreateDomain(domain, request);
        hook.beforeCreate(resourceType(), domain, request);
        D saved = repositoryPort.save(domain);
        hook.afterCreate(resourceType(), saved, request);
        DRS response = toDetailResponse(saved);
        hook.afterCreateResponse(resourceType(), response, saved, request);
        return response;
    }

    @Override
    public DRS update(Long id, URQ request, MutationOptions options) {
        ResourceServiceHook<D, CRQ, URQ, SRS, DRS> hook = serviceHook();
        ResourceMapperHook<D, CRQ, URQ, SRS, DRS> mapperHook = mapperHook();
        hook.beforeUpdateRequest(resourceType(), id, request);
        D existing = findRequired(id);
        return updateExisting(existing, request, hook, mapperHook);
    }

    @Override
    public void delete(Long id, MutationOptions options) {
        ResourceServiceHook<D, CRQ, URQ, SRS, DRS> hook = serviceHook();
        hook.beforeDeleteRequest(resourceType(), id);
        findRequired(id);
        hook.beforeDelete(resourceType(), id);
        repositoryPort.delete(id);
        hook.afterDelete(resourceType(), id);
    }

    private DRS updateExisting(D existing, URQ request,
            ResourceServiceHook<D, CRQ, URQ, SRS, DRS> hook,
            ResourceMapperHook<D, CRQ, URQ, SRS, DRS> mapperHook) {
        mapper.updateDomain(request, existing);
        mapperHook.afterUpdateDomain(existing, request);
        if (request instanceof VersionedRequest vr) {
            existing.setVersion(vr.getVersion());
        }
        hook.beforeUpdate(resourceType(), existing, request);
        D saved = repositoryPort.save(existing);
        hook.afterUpdate(resourceType(), saved, request);
        DRS response = toDetailResponse(saved);
        hook.afterUpdateResponse(resourceType(), response, saved, request);
        return response;
    }

    private D findRequired(Long id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idCodec.encode(id))));
    }

}
