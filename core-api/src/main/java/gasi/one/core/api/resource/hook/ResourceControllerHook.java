package gasi.one.core.api.resource.hook;

import java.util.List;

import gasi.one.core.api.common.dto.ApiResponse;
import gasi.one.core.api.common.dto.PageResult;
import gasi.one.core.api.common.query.QueryRequest;

/**
 * Lifecycle hook contract for resource controller operations.
 *
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type
 * @param <DRS> detail response DTO type
 * @since 1.0.0
 */
public interface ResourceControllerHook<CRQ, URQ, SRS, DRS> {

    default void beforeFindByIdRequest(String id, ResourceRequestContext context) {
    }

    default void afterFindByIdResponse(ApiResponse<DRS> response, String id, ResourceRequestContext context) {
    }

    default void beforeFindByRequest(QueryRequest request, ResourceRequestContext context) {
    }

    default void afterFindByResponse(ApiResponse<DRS> response, QueryRequest request,
            ResourceRequestContext context) {
    }

    default void beforeFindAllRequest(QueryRequest request, ResourceRequestContext context) {
    }

    default void afterFindAllResponse(ApiResponse<List<?>> response, QueryRequest request,
            ResourceRequestContext context) {
    }

    default void beforeFindAllPagedRequest(QueryRequest request, ResourceRequestContext context) {
    }

    default void afterFindAllPagedResponse(ApiResponse<PageResult<?>> response, QueryRequest request,
            ResourceRequestContext context) {
    }

    default void beforeCreateRequest(CRQ request, ResourceRequestContext context) {
    }

    default void afterCreateResponse(ApiResponse<DRS> response, CRQ request, ResourceRequestContext context) {
    }

    default void beforeUpdateRequest(String id, URQ request, ResourceRequestContext context) {
    }

    default void afterUpdateResponse(ApiResponse<DRS> response, String id, URQ request,
            ResourceRequestContext context) {
    }

    default void beforeDeleteRequest(String id, ResourceRequestContext context) {
    }

    default void afterDeleteResponse(ApiResponse<Void> response, String id, ResourceRequestContext context) {
    }

    static <CRQ, URQ, SRS, DRS> ResourceControllerHook<CRQ, URQ, SRS, DRS> noop() {
        return new ResourceControllerHook<>() {
        };
    }

    static <CRQ, URQ, SRS, DRS> ResourceControllerHook<CRQ, URQ, SRS, DRS> composite(
            List<? extends ResourceControllerHook<CRQ, URQ, SRS, DRS>> hooks) {
        if (hooks == null || hooks.isEmpty()) {
            return noop();
        }
        return new ResourceControllerHook<>() {
            @Override
            public void beforeFindByIdRequest(String id, ResourceRequestContext context) {
                hooks.forEach(hook -> hook.beforeFindByIdRequest(id, context));
            }

            @Override
            public void afterFindByIdResponse(ApiResponse<DRS> response, String id,
                    ResourceRequestContext context) {
                hooks.forEach(hook -> hook.afterFindByIdResponse(response, id, context));
            }

            @Override
            public void beforeFindByRequest(QueryRequest request, ResourceRequestContext context) {
                hooks.forEach(hook -> hook.beforeFindByRequest(request, context));
            }

            @Override
            public void afterFindByResponse(ApiResponse<DRS> response, QueryRequest request,
                    ResourceRequestContext context) {
                hooks.forEach(hook -> hook.afterFindByResponse(response, request, context));
            }

            @Override
            public void beforeFindAllRequest(QueryRequest request, ResourceRequestContext context) {
                hooks.forEach(hook -> hook.beforeFindAllRequest(request, context));
            }

            @Override
            public void afterFindAllResponse(ApiResponse<List<?>> response, QueryRequest request,
                    ResourceRequestContext context) {
                hooks.forEach(hook -> hook.afterFindAllResponse(response, request, context));
            }

            @Override
            public void beforeFindAllPagedRequest(QueryRequest request, ResourceRequestContext context) {
                hooks.forEach(hook -> hook.beforeFindAllPagedRequest(request, context));
            }

            @Override
            public void afterFindAllPagedResponse(ApiResponse<PageResult<?>> response, QueryRequest request,
                    ResourceRequestContext context) {
                hooks.forEach(hook -> hook.afterFindAllPagedResponse(response, request, context));
            }

            @Override
            public void beforeCreateRequest(CRQ request, ResourceRequestContext context) {
                hooks.forEach(hook -> hook.beforeCreateRequest(request, context));
            }

            @Override
            public void afterCreateResponse(ApiResponse<DRS> response, CRQ request,
                    ResourceRequestContext context) {
                hooks.forEach(hook -> hook.afterCreateResponse(response, request, context));
            }

            @Override
            public void beforeUpdateRequest(String id, URQ request, ResourceRequestContext context) {
                hooks.forEach(hook -> hook.beforeUpdateRequest(id, request, context));
            }

            @Override
            public void afterUpdateResponse(ApiResponse<DRS> response, String id, URQ request,
                    ResourceRequestContext context) {
                hooks.forEach(hook -> hook.afterUpdateResponse(response, id, request, context));
            }

            @Override
            public void beforeDeleteRequest(String id, ResourceRequestContext context) {
                hooks.forEach(hook -> hook.beforeDeleteRequest(id, context));
            }

            @Override
            public void afterDeleteResponse(ApiResponse<Void> response, String id, ResourceRequestContext context) {
                hooks.forEach(hook -> hook.afterDeleteResponse(response, id, context));
            }
        };
    }
}
