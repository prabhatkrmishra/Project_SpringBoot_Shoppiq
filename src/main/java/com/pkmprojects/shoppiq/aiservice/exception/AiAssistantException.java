package com.pkmprojects.shoppiq.aiservice.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * General-purpose exception for AI assistant errors that do not map to a
 * specific resource-not-found scenario.
 *
 * <p>
 * Wraps various {@link ErrorCode} values with their corresponding HTTP status
 * codes. Factory methods are provided for common failure modes:
 * <ul>
 *   <li>{@link #apiError(String)} — 500 Internal Server Error (general AI failure)</li>
 *   <li>{@link #timeout(String)} — 504 Gateway Timeout (NIM API timeout)</li>
 *   <li>{@link #rateLimited(String)} — 429 Too Many Requests (rate limit exceeded)</li>
 *   <li>{@link #conversationResolved()} — 410 Gone (message sent to resolved conversation)</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Getter
public class AiAssistantException extends ShoppiqException {

    /**
     * Constructs a new {@code AiAssistantException} with the specified error code, HTTP status, and detail.
     *
     * @param errorCode  the machine-readable error code
     * @param httpStatus the HTTP status to return in the response
     * @param detail     a human-readable error description
     */
    public AiAssistantException(ErrorCode errorCode, HttpStatus httpStatus, String detail) {
        super(errorCode, httpStatus, detail);
    }

    /**
     * Creates an exception for a general AI API failure (500).
     *
     * @param detail a human-readable description of the failure
     * @return a new {@code AiAssistantException}
     */
    public static AiAssistantException apiError(String detail) {
        return new AiAssistantException(ErrorCode.AI_API_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR, detail);
    }

    /**
     * Creates an exception for an NIM API timeout (504).
     *
     * @param detail a human-readable description of the timeout
     * @return a new {@code AiAssistantException}
     */
    public static AiAssistantException timeout(String detail) {
        return new AiAssistantException(ErrorCode.AI_TIMEOUT,
                HttpStatus.GATEWAY_TIMEOUT, detail);
    }

    /**
     * Creates an exception for a rate-limited request (429).
     *
     * @param detail a human-readable description of the rate limit
     * @return a new {@code AiAssistantException}
     */
    public static AiAssistantException rateLimited(String detail) {
        return new AiAssistantException(ErrorCode.AI_RATE_LIMITED,
                HttpStatus.TOO_MANY_REQUESTS, detail);
    }

    /**
     * Creates an exception for a message sent to an already-resolved conversation (410).
     *
     * @return a new {@code AiAssistantException}
     */
    public static AiAssistantException conversationResolved() {
        return new AiAssistantException(ErrorCode.AI_CONVERSATION_RESOLVED,
                HttpStatus.GONE,
                "This conversation has been resolved. Please start a new conversation.");
    }
}
