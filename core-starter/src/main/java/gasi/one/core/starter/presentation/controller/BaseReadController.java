package gasi.one.core.starter.presentation.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import gasi.one.core.api.common.dto.PageResult;
import gasi.one.core.api.resource.port.inbound.BaseReadService;
import gasi.one.core.api.resource.hook.ResourceControllerHook;
import gasi.one.core.api.common.dto.ApiResponse;
import gasi.one.core.api.common.query.QueryRequest;
import gasi.one.core.starter.application.hook.ResourceControllerHookRegistry;
import gasi.one.core.starter.presentation.support.ResponseProjection;
import gasi.one.core.api.common.id.IdCodec;

/**
 * Abstract REST controller for standard read and query endpoints.
 *
 * <p>
 * Subclasses only need to supply the concrete {@link BaseReadService} via
 * the constructor and annotate with {@code @RestController} and
 * {@code @RequestMapping}.
 * </p>
 *
 * <h2>Exposed endpoints relative to the subclass mapping</h2>
 * <table>
 * <caption>Default read and query endpoints</caption>
 * <tr>
 * <th>Method</th>
 * <th>Path</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>GET</td>
 * <td>/{id}</td>
 * <td>Get resource by ID</td>
 * </tr>
 * <tr>
 * <td>POST</td>
 * <td>/query/one</td>
 * <td>Find single resource by filter</td>
 * </tr>
 * <tr>
 * <td>POST</td>
 * <td>/query/list</td>
 * <td>Find all matching (list)</td>
 * </tr>
 * <tr>
 * <td>POST</td>
 * <td>/query/page</td>
 * <td>Find all matching (paged)</td>
 * </tr>
 * </table>
 *
 * @param <SRS> summary response DTO type (for lists)
 * @param <DRS> detail response DTO type (for single entity)
 * @since 1.0.0
 */
public abstract class BaseReadController<SRS, DRS> {

    /**
     * Returns the resource type used by permission checks and controller hooks.
     * For example, returning "ROLE" will evaluate permissions like "ROLE:CREATE".
     *
     * @return the resource type.
     */
    public abstract String resourceType();

    private final BaseReadService<SRS, DRS> service;
    private final IdCodec idCodec;
    private final ResourceControllerHookRegistry hookRegistry;

    /**
     * Constructs a new {@code BaseReadController} with ordered controller hooks.
     *
     * @param service      the service handling business logic
     * @param idCodec      the ID codec for encoding/decoding IDs
     * @param hookRegistry registry for generated and custom controller hooks
     */
    protected BaseReadController(BaseReadService<SRS, DRS> service,
            IdCodec idCodec,
            ResourceControllerHookRegistry hookRegistry) {
        this.service = service;
        this.idCodec = idCodec;
        this.hookRegistry = hookRegistry;
    }

    /**
     * Returns the underlying service instance.
     *
     * @return the base read service
     */
    protected BaseReadService<SRS, DRS> getService() {
        return service;
    }

    /**
     * Returns the ID codec instance.
     *
     * @return the ID codec
     */
    protected IdCodec getIdCodec() {
        return idCodec;
    }

    /**
     * Default response projection for query list/page when callers omit fields.
     *
     * <p>
     * Subclasses should override this with resource-specific default table
     * columns. The fallback keeps only {@code id} to avoid accidentally exposing
     * a full summary DTO on unprojected requests.
     * </p>
     *
     * @return default public DTO field names
     */
    protected List<String> getDefaultSummaryFields() {
        return List.of("id");
    }

    private List<String> resolveProjectionFields(QueryRequest request) {
        return request.getFields() == null || request.getFields().isEmpty()
                ? getDefaultSummaryFields()
                : request.getFields();
    }

    /**
     * Retrieves a resource by its identifier.
     *
     * @param id the resource identifier
     * @return the resource detail wrapped in {@link ApiResponse}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<DRS> findById(@PathVariable String id) {
        ResourceControllerHook<Object, Object, SRS, DRS> hook = controllerHook();
        hook.beforeFindByIdRequest(id);
        ApiResponse<DRS> response = ApiResponse.ok(service.findById(idCodec.decode(id)));
        hook.afterFindByIdResponse(response, id);
        return response;
    }

    /**
     * Finds a single resource matching the given filter.
     *
     * @param request the query request containing filter criteria
     * @return the matching resource detail wrapped in {@link ApiResponse}
     */
    @PostMapping("/query/one")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<DRS> findBy(@RequestBody QueryRequest request) {
        ResourceControllerHook<Object, Object, SRS, DRS> hook = controllerHook();
        hook.beforeFindByRequest(request);
        ApiResponse<DRS> response = ApiResponse.ok(service.findBy(request.getFilter()));
        hook.afterFindByResponse(response, request);
        return response;
    }

    /**
     * Finds all resources matching the given filter and sort orders.
     *
     * @param request the query request containing filter and sort criteria
     * @return a list of matching resource summaries wrapped in {@link ApiResponse}
     */
    @PostMapping("/query/list")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<List<?>> findAll(@RequestBody QueryRequest request) {
        ResourceControllerHook<Object, Object, SRS, DRS> hook = controllerHook();
        hook.beforeFindAllRequest(request);
        List<SRS> result = service.findAll(
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());
        ApiResponse<List<?>> response = ApiResponse.ok(ResponseProjection.projectList(result, resolveProjectionFields(request)));
        hook.afterFindAllResponse(response, request);
        return response;
    }

    /**
     * Finds all resources matching the given filter with pagination.
     *
     * @param request the query request containing filter and sort criteria
     * @return a page of matching resource summaries
     */
    @PostMapping("/query/page")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<PageResult<?>> findAllPaged(
            @RequestBody QueryRequest request) {
        ResourceControllerHook<Object, Object, SRS, DRS> hook = controllerHook();
        hook.beforeFindAllPagedRequest(request);
        PageResult<SRS> result = service.findAll(
                request.normalizedPage(),
                request.normalizedSize(),
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());
        ApiResponse<PageResult<?>> response = ApiResponse.ok(ResponseProjection.projectPage(result, resolveProjectionFields(request)));
        hook.afterFindAllPagedResponse(response, request);
        return response;
    }

    /**
     * Finds lookup rows with pagination and lookup-specific projection defaults.
     *
     * @param request the query request containing filter, sort, pagination,
     *                and optional projection fields
     * @return a projected page of lookup rows wrapped in {@link ApiResponse}
     */
    @PostMapping("/lookup/query/page")
    @PreAuthorize("hasPermission(this, 'LOOKUP')")
    public ApiResponse<PageResult<?>> lookupPaged(@RequestBody QueryRequest request) {
        ResourceControllerHook<Object, Object, SRS, DRS> hook = controllerHook();
        hook.beforeLookupPagedRequest(request);
        PageResult<SRS> result = service.findAll(
                request.normalizedPage(),
                request.normalizedSize(),
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());

        ApiResponse<PageResult<?>> response = ApiResponse.ok(ResponseProjection.projectPage(result, resolveLookupFields(request)));
        hook.afterLookupPagedResponse(response, request);
        return response;
    }

    /**
     * Default response projection for lookup endpoints when callers omit fields.
     *
     * @return default public DTO field names for lookup responses
     */
    protected List<String> getDefaultLookupFields() {
        return getDefaultSummaryFields();
    }

    private List<String> resolveLookupFields(QueryRequest request) {
        return request.getFields() == null || request.getFields().isEmpty()
                ? getDefaultLookupFields()
                : request.getFields();
    }

    protected <CRQ, URQ> ResourceControllerHook<CRQ, URQ, SRS, DRS> controllerHook() {
        if (hookRegistry == null) {
            return ResourceControllerHook.noop();
        }
        return hookRegistry.resolve(resourceType());
    }
}
