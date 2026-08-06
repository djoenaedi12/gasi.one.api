package gasi.one.core.api.audit;

/**
 * Service contract for writing audit logs outside of automatic auditing.
 *
 * <p>Use this service for explicit business events that are not naturally covered
 * by CRUD interception, such as login, export, approval, or integration
 * callbacks.</p>
 *
 * @since 1.0.0
 */
public interface AuditLogService {

    /**
     * Write a manual audit log entry.
     *
     * @param entry audit log entry
     */
    void log(AuditLogEntry entry);
}
