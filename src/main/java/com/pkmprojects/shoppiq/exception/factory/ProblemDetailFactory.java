package com.pkmprojects.shoppiq.exception.factory;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.constants.ProblemDetailProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

/**
 * Factory responsible for creating RFC 9457 compliant {@link ProblemDetail}
 * instances for the Shoppiq application.
 *
 * <p>
 * This utility centralizes the creation of API error responses to ensure
 * a consistent error structure throughout the application.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create RFC 9457 compliant {@link ProblemDetail} responses.</li>
 *     <li>Populate application-specific metadata.</li>
 *     <li>Provide reusable factory methods for application and framework exceptions.</li>
 * </ul>
 *
 * <h2>Custom Properties</h2>
 * <ul>
 *     <li>timestamp</li>
 *     <li>errorCode</li>
 * </ul>
 *
 * <h2>Future Scope</h2>
 * <ul>
 *     <li>Trace Identifier (traceId)</li>
 *     <li>Correlation Identifier (correlationId)</li>
 *     <li>Error documentation URI</li>
 *     <li>Localized error messages</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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