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
import gasi.one.core.api.resource.hook.ResourceRepositoryHook;
import gasi.one.core.api.resource.model.BaseModel;

/**
 * Registry for ordered resource repository hooks.
 *
 * @since 1.0.0
 */
@Component
public class ResourceRepositoryHookRegistry {

    private final Map<String, List<ResourceRepositoryHook<?>>> hooksByResource;

    public ResourceRepositoryHookRegistry(List<ResourceRepositoryHook<?>> hooks) {
        List<ResourceRepositoryHook<?>> orderedHooks = new ArrayList<>(hooks);
        AnnotationAwareOrderComparator.sort(orderedHooks);

        Map<String, List<ResourceRepositoryHook<?>>> grouped = new HashMap<>();
        for (ResourceRepositoryHook<?> hook : orderedHooks) {
            ResourceHook annotation = AnnotatedElementUtils.findMergedAnnotation(
                    hook.getClass(), ResourceHook.class);
            if (annotation == null || annotation.layer() != HookLayer.REPOSITORY || annotation.value().isBlank()) {
                continue;
            }
            grouped.computeIfAbsent(annotation.value(), ignored -> new ArrayList<>()).add(hook);
        }
        this.hooksByResource = Map.copyOf(grouped);
    }

    public <D extends BaseModel> ResourceRepositoryHook<D> resolve(String resourceType) {
        List<ResourceRepositoryHook<?>> globalHooks = hooksByResource.get(ResourceHook.ANY_RESOURCE);
        List<ResourceRepositoryHook<?>> hooks = hooksByResource.get(resourceType);
        if ((globalHooks == null || globalHooks.isEmpty()) && (hooks == null || hooks.isEmpty())) {
            return ResourceRepositoryHook.noop();
        }

        List<ResourceRepositoryHook<D>> typedHooks = new ArrayList<>();
        if (globalHooks != null) {
            for (ResourceRepositoryHook<?> hook : globalHooks) {
                typedHooks.add(cast(hook));
            }
        }
        if (hooks != null) {
            for (ResourceRepositoryHook<?> hook : hooks) {
                typedHooks.add(cast(hook));
            }
        }
        AnnotationAwareOrderComparator.sort(typedHooks);
        return ResourceRepositoryHook.composite(typedHooks);
    }

    @SuppressWarnings("unchecked")
    private static <D extends BaseModel> ResourceRepositoryHook<D> cast(ResourceRepositoryHook<?> hook) {
        return (ResourceRepositoryHook<D>) hook;
    }
}
