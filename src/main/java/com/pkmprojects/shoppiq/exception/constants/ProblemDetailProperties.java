package com.pkmprojects.shoppiq.exception.constants;

/**
 * <strong>Spring Boot Concept:</strong> Centralised constants class for
 * RFC 9457 {@link org.springframework.http.ProblemDetail} custom properties.
 *
 * <p>These properties supplement the standard RFC 9457 fields with
 * application-specific metadata ({@code timestamp}, {@code errorCode}).
 * Defining them as constants prevents typos and ensures consistency
 * across {@link com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory},
 * exception handlers, and clients that read these fields.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class ProblemDetailProperties {

    /**
     * Timestamp indicating when the error occurred.
     */
    public static final String TIMESTAMP = "timestamp";

    /**
     * Stable machine-readable application error code.
     */
    public static final String ERROR_CODE = "errorCode";

    /**
     * Prevents instantiation.
     */
    private ProblemDetailProperties() {
        throw new UnsupportedOperationException("ProblemDetailProperties is a utility class and cannot be instantiated.");
    }
}
