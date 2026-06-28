package com.pkmprojects.shoppiq.exception.constants;

/**
 * Defines custom property names added to RFC 9457
 * {@link org.springframework.http.ProblemDetail} responses.
 *
 * <p>
 * These properties supplement the standard RFC 9457 fields
 * and provide additional metadata required by Shoppiq clients.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Centralize custom ProblemDetail property names.</li>
 *     <li>Prevent duplicated string literals.</li>
 *     <li>Ensure consistency across the application.</li>
 * </ul>
 *
 * <h2>Current Properties</h2>
 * <ul>
 *     <li>{@code timestamp}</li>
 *     <li>{@code errorCode}</li>
 * </ul>
 *
 * <h2>Future Scope</h2>
 * <ul>
 *     <li>traceId</li>
 *     <li>correlationId</li>
 *     <li>requestId</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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