package gasi.one.core.starter.application.support;

import org.springframework.stereotype.Component;

import gasi.one.core.api.common.exception.BusinessException;
import gasi.one.core.api.common.id.IdCodec;
import gasi.one.core.api.resource.model.BaseModel;
import gasi.one.core.api.resource.port.outbound.BaseRepositoryPort;

/**
 * Resolves encoded request IDs into domain references while collecting
 * validation errors.
 *
 * <p>This helper is intended for application services that need to populate
 * Many-to-One references from request DTO ID fields.</p>
 *
 * @since 1.0.0
 */
@Component
public class ReferenceResolver {

    private final IdCodec idCodec;

    /**
     * Creates a reference resolver.
     *
     * @param idCodec public ID codec
     */
    public ReferenceResolver(IdCodec idCodec) {
        this.idCodec = idCodec;
    }

    /**
     * Resolves a single encoded ID through the supplied repository port.
     *
     * <p>Blank values are ignored and return {@code null}; request DTO
     * annotations such as {@code @NotBlank} should handle requiredness before
     * service execution.</p>
     *
     * @param port      repository port for the referenced aggregate
     * @param encodedId encoded public ID from the request
     * @param fieldName request field name used in validation errors
     * @param collector error collector
     * @param <T>       referenced domain model type
     * @return resolved model, or {@code null} if blank or not found
     */
    public <T extends BaseModel> T resolve(
            BaseRepositoryPort<T> port,
            String encodedId,
            String fieldName,
            BusinessException.Collector collector) {
        if (encodedId == null || encodedId.isBlank()) {
            return null;
        }

        Long id = idCodec.decode(encodedId);
        return port.findById(id)
                .orElseGet(() -> {
                    collector.add("Invalid " + fieldName + ": " + encodedId);
                    return null;
                });
    }
}
