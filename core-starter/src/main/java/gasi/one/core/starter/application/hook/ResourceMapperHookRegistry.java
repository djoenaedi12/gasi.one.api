package gasi.one.core.starter.application.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import gasi.one.core.api.resource.hook.HookLayer;
import gasi.one.core.api.resource.hook.ResourceHook;
import gasi.one.core.api.resource.hook.ResourceMapperHook;
import gasi.one.core.api.resource.model.BaseModel;

/**
 * Registry for ordered resource mapper hooks.
 *
 * @since 1.0.0
 */
@Component
public class ResourceMapperHookRegistry {

    private final Map<String, List<ResourceMapperHook<?, ?, ?, ?, ?>>> hooksByResource;

    public ResourceMapperHookRegistry(List<ResourceMapperHook<?, ?, ?, ?, ?>> hooks) {
        List<ResourceMapperHook<?, ?, ?, ?, ?>> orderedHooks = new ArrayList<>(hooks);
        AnnotationAwareOrderComparator.sort(orderedHooks);

        Map<String, List<ResourceMapperHook<?, ?, ?, ?, ?>>> grouped = new HashMap<>();
        for (ResourceMapperHook<?, ?, ?, ?, ?> hook : orderedHooks) {
            ResourceHook annotation = AnnotatedElementUtils.findMergedAnnotation(
                    hook.getClass(), ResourceHook.class);
            if (annotation == null || annotation.layer() != HookLayer.MAPPER || annotation.value().isBlank()) {
                continue;
            }
            grouped.computeIfAbsent(annotation.value(), ignored -> new ArrayList<>()).add(hook);
        }
        this.hooksByResource = Map.copyOf(grouped);
    }

    public <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceMapperHook<D, CRQ, URQ, SRS, DRS> resolve(
            String resourceType) {
        List<ResourceMapperHook<?, ?, ?, ?, ?>> globalHooks = hooksByResource.get(ResourceHook.ANY_RESOURCE);
        List<ResourceMapperHook<?, ?, ?, ?, ?>> hooks = hooksByResource.get(resourceType);
        if ((globalHooks == null || globalHooks.isEmpty()) && (hooks == null || hooks.isEmpty())) {
            return ResourceMapperHook.noop();
        }

        List<ResourceMapperHook<D, CRQ, URQ, SRS, DRS>> typedHooks = new ArrayList<>();
        if (globalHooks != null) {
            for (ResourceMapperHook<?, ?, ?, ?, ?> hook : globalHooks) {
                typedHooks.add(cast(hook));
            }
        }
        if (hooks != null) {
            for (ResourceMapperHook<?, ?, ?, ?, ?> hook : hooks) {
                typedHooks.add(cast(hook));
            }
        }
        AnnotationAwareOrderComparator.sort(typedHooks);
        return ResourceMapperHook.composite(typedHooks);
    }

    @SuppressWarnings("unchecked")
    private static <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceMapperHook<D, CRQ, URQ, SRS, DRS> cast(
            ResourceMapperHook<?, ?, ?, ?, ?> hook) {
        return (ResourceMapperHook<D, CRQ, URQ, SRS, DRS>) hook;
    }
}
