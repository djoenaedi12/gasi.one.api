package gasi.one.platform.bootstrap;

/**
 * Centralized package scan roots used by the platform host.
 *
 * @since 1.0.0
 */
public final class PlatformScanPackages {

    /** Platform host package. */
    public static final String PLATFORM = "gasi.one.platform";

    /** Shared Spring/JPA implementation package. */
    public static final String CORE_STARTER = "gasi.one.core.starter";

    /** Root package used by generated API plugins. */
    public static final String PLUGIN_ROOT = "gasi.one.plugins";

    private PlatformScanPackages() {
    }
}
