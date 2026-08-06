package gasi.one.core.starter.application.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import gasi.one.core.api.resource.hook.HookLayer;
import gasi.one.core.api.resource.hook.ResourceControllerHook;
import gasi.one.core.api.resource.hook.ResourceHook;

/**
 * Registry for ordered resource controller hooks.
 *
 * @since 1.0.0
 */
@Component
public class ResourceControllerHookRegistry {

    private final Map<String, List<ResourceControllerHook<?, ?, ?, ?>>> hooksByResource;

    public ResourceControllerHookRegistry(List<ResourceControllerHook<?, ?, ?, ?>> hooks) {
        List<ResourceControllerHook<?, ?, ?, ?>> orderedHooks = new ArrayList<>(hooks);
        AnnotationAwareOrderComparator.sort(orderedHooks);

        Map<String, List<ResourceControllerHook<?, ?, ?, ?>>> grouped = new HashMap<>();
        for (ResourceControllerHook<?, ?, ?, ?> hook : orderedHooks) {
            ResourceHook annotation = AnnotatedElementUtils.findMergedAnnotation(
                    hook.getClass(), ResourceHook.class);
            if (annotation == null || annotation.layer() != HookLayer.CONTROLLER || annotation.value().isBlank()) {
                continue;
            }
            grouped.computeIfAbsent(annotation.value(), ignored -> new ArrayList<>()).add(hook);
        }
        this.hooksByResource = Map.copyOf(grouped);
    }

    public <CRQ, URQ, SRS, DRS> ResourceControllerHook<CRQ, URQ, SRS, DRS> resolve(String resourceType) {
        List<ResourceControllerHook<?, ?, ?, ?>> globalHooks = hooksByResource.get(ResourceHook.ANY_RESOURCE);
        List<ResourceControllerHook<?, ?, ?, ?>> hooks = hooksByResource.get(resourceType);
        if ((globalHooks == null || globalHooks.isEmpty()) && (hooks == null || hooks.isEmpty())) {
            return ResourceControllerHook.noop();
        }

        List<ResourceControllerHook<CRQ, URQ, SRS, DRS>> typedHooks = new ArrayList<>();
        if (globalHooks != null) {
            for (ResourceControllerHook<?, ?, ?, ?> hook : globalHooks) {
                typedHooks.add(cast(hook));
            }
        }
        if (hooks != null) {
            for (ResourceControllerHook<?, ?, ?, ?> hook : hooks) {
                typedHooks.add(cast(hook));
            }
        }
        AnnotationAwareOrderComparator.sort(typedHooks);
        return ResourceControllerHook.composite(typedHooks);
    }

    @SuppressWarnings("unchecked")
    private static <CRQ, URQ, SRS, DRS> ResourceControllerHook<CRQ, URQ, SRS, DRS> cast(
            ResourceControllerHook<?, ?, ?, ?> hook) {
        return (ResourceControllerHook<CRQ, URQ, SRS, DRS>) hook;
    }
}
