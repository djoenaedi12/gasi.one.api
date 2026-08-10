package gasi.one.core.api.resource.hook;

/**
 * Thread-bound holder for the current resource request context.
 *
 * <p>
 * The web layer is responsible for setting and clearing this holder around a
 * request. Consumers in deeper layers can read the current context without
 * changing every service or repository method signature.
 * </p>
 *
 * @since 1.0.0
 */
public final class ResourceRequestContextHolder {

    private static final ThreadLocal<ResourceRequestContext> CURRENT = new ThreadLocal<>();

    private ResourceRequestContextHolder() {
    }

    public static void set(ResourceRequestContext context) {
        CURRENT.set(context == null ? ResourceRequestContext.empty() : context);
    }

    public static ResourceRequestContext get() {
        ResourceRequestContext context = CURRENT.get();
        return context == null ? ResourceRequestContext.empty() : context;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
