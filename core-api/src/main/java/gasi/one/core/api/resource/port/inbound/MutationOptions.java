package gasi.one.core.api.resource.port.inbound;

/**
 * Options for write operations.
 *
 * @since 1.0.0
 */
public record MutationOptions() {

    /**
     * Returns default mutation options.
     *
     * @return default options
     */
    public static MutationOptions defaults() {
        return new MutationOptions();
    }
}
