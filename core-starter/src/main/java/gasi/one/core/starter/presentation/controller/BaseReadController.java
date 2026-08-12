package gasi.one.core.starter.presentation.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;

import gasi.one.core.api.common.exception.BadRequestException;
import gasi.one.core.api.common.exception.BusinessException;
import gasi.one.core.api.common.exception.ErrorDetail;
import gasi.one.core.api.common.dto.PageResult;
import gasi.one.core.api.resource.port.inbound.BaseReadService;
import gasi.one.core.api.resource.hook.ResourceRequestContext;
import gasi.one.core.api.resource.hook.ResourceRequestContextHolder;
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
     * Subclasses should override this with resource-specific projection fields.
     * The fallback keeps only {@code id} to avoid accidentally exposing a full
     * summary DTO on unprojected requests.
     * </p>
     *
     * @return default public DTO field names
     */
    protected List<String> getDefaultProjectionFields() {
        return List.of("id");
    }

    private List<String> resolveProjectionFields(QueryRequest request) {
        return request.getFields() == null || request.getFields().isEmpty()
                ? getDefaultProjectionFields()
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
        ResourceRequestContext context = requestContext();
        ResourceRequestContextHolder.set(context);
        try {
            hook.beforeFindByIdRequest(id, context);
            ApiResponse<DRS> response = ApiResponse.ok(service.findById(decodeId(id)));
            hook.afterFindByIdResponse(response, id, context);
            return response;
        } finally {
            ResourceRequestContextHolder.clear();
        }
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
        ResourceRequestContext context = requestContext();
        ResourceRequestContextHolder.set(context);
        try {
            requireSingleQueryFilter(request);
            hook.beforeFindByRequest(request, context);
            ApiResponse<DRS> response = ApiResponse.ok(service.findBy(request.getFilter()));
            hook.afterFindByResponse(response, request, context);
            return response;
        } finally {
            ResourceRequestContextHolder.clear();
        }
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
        ResourceRequestContext context = requestContext();
        ResourceRequestContextHolder.set(context);
        try {
            hook.beforeFindAllRequest(request, context);
            List<SRS> result = service.findAll(
                    request.getFilter(),
                    request.getSorts() != null ? request.getSorts() : Collections.emptyList());
            ApiResponse<List<?>> response = ApiResponse.ok(ResponseProjection.projectList(result, resolveProjectionFields(request)));
            hook.afterFindAllResponse(response, request, context);
            return response;
        } finally {
            ResourceRequestContextHolder.clear();
        }
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
        ResourceRequestContext context = requestContext();
        ResourceRequestContextHolder.set(context);
        try {
            hook.beforeFindAllPagedRequest(request, context);
            PageResult<SRS> result = service.findAll(
                    request.normalizedPage(),
                    request.normalizedSize(),
                    request.getFilter(),
                    request.getSorts() != null ? request.getSorts() : Collections.emptyList());
            ApiResponse<PageResult<?>> response = ApiResponse.ok(ResponseProjection.projectPage(result, resolveProjectionFields(request)));
            hook.afterFindAllPagedResponse(response, request, context);
            return response;
        } finally {
            ResourceRequestContextHolder.clear();
        }
    }

    /**
     * Decodes a public resource ID into its internal numeric ID.
     *
     * @param id public encoded identifier
     * @return decoded internal numeric identifier
     */
    protected Long decodeId(String id) {
        try {
            Long decoded = idCodec.decode(id);
            if (decoded == null) {
                throw invalidId();
            }
            return decoded;
        } catch (IllegalArgumentException ex) {
            throw invalidId();
        }
    }

    private BadRequestException invalidId() {
        return BadRequestException.of(ErrorDetail.of(
                "INVALID_ID",
                "id",
                "error.id.invalid"));
    }

    private void requireSingleQueryFilter(QueryRequest request) {
        if (request == null || request.getFilter() == null) {
            throw BusinessException.of(ErrorDetail.of(
                    "QUERY_ONE_FILTER_REQUIRED",
                    "filter",
                    "error.query.one.filterRequired"));
        }
    }

    protected <CRQ, URQ> ResourceControllerHook<CRQ, URQ, SRS, DRS> controllerHook() {
        if (hookRegistry == null) {
            return ResourceControllerHook.noop();
        }
        return hookRegistry.resolve(resourceType());
    }

    protected ResourceRequestContext requestContext() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return ResourceRequestContext.empty();
        }
        HttpServletRequest request = attributes.getRequest();
        Object value = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Map<?, ?> pathVariables = value instanceof Map<?, ?> variables ? variables : Map.of();

        return ResourceRequestContext.builder()
                .pathVariables(pathVariables)
                .method(request.getMethod())
                .requestUri(request.getRequestURI())
                .requestUrl(request.getRequestURL().toString())
                .queryString(request.getQueryString())
                .contextPath(request.getContextPath())
                .servletPath(request.getServletPath())
                .build();
    }
}
