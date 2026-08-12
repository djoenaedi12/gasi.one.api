package gasi.one.core.api.common.exception;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception raised when the client sends an invalid request.
 *
 * @since 1.0.0
 */
public class BadRequestException extends RuntimeException {

    private final List<ErrorDetail> details;

    /**
     * Creates an exception from structured error details.
     *
     * @param details structured error items
     */
    public BadRequestException(List<ErrorDetail> details) {
        super(details.stream().map(ErrorDetail::getMessage).collect(Collectors.joining("; ")));
        this.details = List.copyOf(details);
    }

    /**
     * Creates an exception from a single structured error detail.
     *
     * @param detail structured error item
     * @return a new {@code BadRequestException}
     */
    public static BadRequestException of(ErrorDetail detail) {
        return new BadRequestException(List.of(detail));
    }

    /**
     * Returns the structured error details.
     *
     * @return immutable list of {@link ErrorDetail}
     */
    public List<ErrorDetail> getErrorDetails() {
        return List.copyOf(details);
    }
}
