package gasi.one.core.api.resource.model;

/**
 * Lifecycle state for domain models that support soft deletion or activation.
 *
 * @since 1.0.0
 */
public enum LifecycleStatus {
    /** Entity is saved as a draft and not active yet. */
    DRAFT,
    /** Entity is available for normal use. */
    ACTIVE,
    /** Entity is retained but disabled for normal use. */
    INACTIVE,
    /** Entity is waiting for an external workflow. */
    PENDING,
    /** Entity was rejected by an external workflow. */
    REJECTED,
    /** Entity has been soft-deleted and should be excluded from normal views. */
    DELETED
}
