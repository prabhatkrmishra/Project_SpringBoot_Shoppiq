package com.pkmprojects.shoppiq.exception.factory;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.constants.ProblemDetailProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;

/**
 * Utility class for creating RFC 9457 compliant {@link ProblemDetail} instances.
 *
 * <p>This factory centralizes the creation of Problem Detail responses,
 * ensuring that every error response includes the required fields: HTTP
 * status, title (derived from the status reason phrase), detail message,
 * instance URI, timestamp, and application-specific error code. The
 * factory is used exclusively by the
 * {@link com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler}
 * to produce consistent error responses across the entire API.</p>
 *
 * <p>The factory supports two creation modes: one that accepts a
 * {@link ShoppiqException} directly (extracting all fields from the
 * exception) and another that accepts individual parameters for framework
 * or unexpected exceptions. Both modes delegate to a private
 * {@link #createProblemDetail} method that populates the Problem Detail
 * with the standard properties.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler
 * @since 1.0.0
 */
public final class ProblemDetailFactory {

    private static Clock clock = Clock.systemUTC();

    /**
     * Prevents instantiation.
     */
    private ProblemDetailFactory() {
        throw new UnsupportedOperationException("ProblemDetailFactory is a utility class and cannot be instantiated.");
    }

    /**
     * Sets the clock used for timestamps in Problem Detail responses.
     *
     * <p>This method is intended for test use only, allowing tests to provide
     * a fixed clock that produces deterministic timestamps. In production,
     * the default system UTC clock is used. The clock is package-private
     * to prevent accidental use in production code.</p>
     *
     * @param clock the clock to use for timestamp generation
     */
    static void setClock(Clock clock) {
        ProblemDetailFactory.clock = clock;
    }

    /**
     * Creates a {@link ProblemDetail} from a {@link ShoppiqException}.
     *
     * <p>This is the primary factory method used by the global exception
     * handler for application-specific exceptions. It extracts the HTTP
     * status, detail message, and error code from the exception and
     * delegates to {@link #createProblemDetail} to populate the standard
     * properties.</p>
     *
     * @param exception the application-specific exception
     * @param instance  the request URI identifying this error occurrence
     * @return a fully populated {@link ProblemDetail} instance
     */
    public static ProblemDetail create(ShoppiqException exception, URI instance) {
        return createProblemDetail(exception.getHttpStatus(), exception.getDetail(), exception.getErrorCode(), instance);
    }

    /**
     * Creates a {@link ProblemDetail} for framework or unexpected exceptions.
     *
     * <p>This factory method is used when the exception is not a
     * {@link ShoppiqException} and the caller must provide the HTTP status,
     * detail message, and error code explicitly. It is typically called by
     * the global exception handler for validation errors, constraint
     * violations, and other Spring framework exceptions.</p>
     *
     * @param status    the HTTP status for the response
     * @param detail    the human-readable error detail
     * @param errorCode the application-specific error code
     * @param instance  the request URI identifying this error occurrence
     * @return a fully populated {@link ProblemDetail} instance
     */
    public static ProblemDetail create(HttpStatus status, String detail, ErrorCode errorCode, URI instance) {
        return createProblemDetail(status, detail, errorCode, instance);
    }

    /**
     * Creates and populates a {@link ProblemDetail} with all standard properties.
     *
     * <p>This private method is the single point of Problem Detail creation.
     * It sets the HTTP status, detail message, title (derived from the
     * status reason phrase), instance URI, timestamp (using the configured
     * clock), and application-specific error code. The timestamp is
     * included as a custom property under the key defined in
     * {@link ProblemDetailProperties#TIMESTAMP}.</p>
     *
     * @param status    the HTTP status for the response
     * @param detail    the human-readable error detail
     * @param errorCode the application-specific error code
     * @param instance  the request URI identifying this error occurrence
     * @return a fully populated {@link ProblemDetail} instance
     */
    private static ProblemDetail createProblemDetail(HttpStatus status, String detail, ErrorCode errorCode, URI instance) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);

        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(instance);
        problemDetail.setProperty(ProblemDetailProperties.TIMESTAMP, Instant.now(clock));
        problemDetail.setProperty(ProblemDetailProperties.ERROR_CODE, errorCode.getCode());

        return problemDetail;
    }

}
