package gasi.one.core.api.resource.hook;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a hook as applying to a generated resource type and layer.
 *
 * <p>
 * Hooks with the same resource type and layer are executed according to
 * Spring's {@code @Order} semantics by the matching starter hook registry.
 * </p>
 *
 * @since 1.0.0
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ResourceHook {

    /**
     * Marker value for hooks that apply to every resource type.
     */
    String ANY_RESOURCE = "*";

    /**
     * Resource type handled by the hook, for example {@code Employee}, or
     * {@link #ANY_RESOURCE} for global hooks.
     *
     * @return resource type name
     */
    String value();

    /**
     * Resource layer handled by the hook.
     *
     * @return hook layer
     */
    HookLayer layer() default HookLayer.SERVICE;
}
