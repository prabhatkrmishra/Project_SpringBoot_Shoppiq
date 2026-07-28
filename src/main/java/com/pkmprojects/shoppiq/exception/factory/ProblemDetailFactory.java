package com.pkmprojects.shoppiq.exception.factory;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.constants.ProblemDetailProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Utility class responsible for
 * creating RFC 9457 compliant {@link ProblemDetail} instances.
 *
 * <p>Centralizes the creation of API error responses to ensure a consistent
 * error structure throughout the application. Consumed by the global
 * {@code @ControllerAdvice} exception handler and by filters
 * (e.g. {@link com.pkmprojects.shoppiq.filter.RateLimitFilter}) that need
 * to write error responses directly. Overloaded {@code create} methods
 * support both {@link com.pkmprojects.shoppiq.exception.base.ShoppiqException}
 * (self-contained status and error code) and framework-level exceptions
 * (status and code supplied explicitly).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class ProblemDetailFactory {

    /**
     * Prevents instantiation.
     */
    private ProblemDetailFactory() {
        throw new UnsupportedOperationException("ProblemDetailFactory is a utility class and cannot be instantiated.");
    }

    /**
     * Creates a {@link ProblemDetail} create a {@link ShoppiqException}.
     *
     * @param exception application-specific exception
     * @param instance  request URI
     * @return populated {@link ProblemDetail}
     */
    public static ProblemDetail create(ShoppiqException exception, URI instance) {
        return createProblemDetail(exception.getHttpStatus(), exception.getDetail(), exception.getErrorCode(), instance);
    }

    /**
     * Creates a {@link ProblemDetail} for framework or unexpected exceptions.
     *
     * @param status    HTTP status
     * @param detail    error detail
     * @param errorCode application error code
     * @param instance  request URI
     * @return populated {@link ProblemDetail}
     */
    public static ProblemDetail create(HttpStatus status, String detail, ErrorCode errorCode, URI instance) {
        return createProblemDetail(status, detail, errorCode, instance);
    }

    /**
     * Creates and populates a {@link ProblemDetail}.
     *
     * @param status    HTTP status
     * @param detail    detailed error message
     * @param errorCode application error code
     * @param instance  request URI
     * @return populated {@link ProblemDetail}
     */
    private static ProblemDetail createProblemDetail(HttpStatus status, String detail, ErrorCode errorCode, URI instance) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);

        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(instance);
        problemDetail.setProperty(ProblemDetailProperties.TIMESTAMP, Instant.now());
        problemDetail.setProperty(ProblemDetailProperties.ERROR_CODE, errorCode.getCode());

        return problemDetail;
    }

}
