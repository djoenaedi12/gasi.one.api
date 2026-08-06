package gasi.one.core.api.audit;

/**
 * Manual audit log entry submitted by application or plugin code.
 *
 * @param action       audited action, for example {@code CREATE}, {@code EXPORT},
 *                     or {@code LOGIN}
 * @param module       module code that owns the event
 * @param resourceType audited resource type, or {@code null} for non-resource
 *                     events
 * @param resourceId   audited resource identifier, or {@code null} for
 *                     non-resource events
 * @param description  human-readable audit description
 * @since 1.0.0
 */
public record AuditLogEntry(
        String action,
        String module,
        String resourceType,
        String resourceId,
        String description) {

    /**
     * Creates a manual audit log entry.
     *
     * @param action       audited action
     * @param module       module code that owns the event
     * @param resourceType audited resource type, or {@code null}
     * @param resourceId   audited resource identifier, or {@code null}
     * @param description  human-readable audit description
     * @return audit log entry
     */
    public static AuditLogEntry of(
            String action,
            String module,
            String resourceType,
            String resourceId,
            String description) {
        return new AuditLogEntry(action, module, resourceType, resourceId, description);
    }
}
