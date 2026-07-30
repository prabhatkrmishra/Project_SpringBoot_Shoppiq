package com.pkmprojects.shoppiq.aiservice.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * General-purpose exception for AI assistant errors with various HTTP status mappings.
 *
 * <p>This exception serves as the primary error type for AI-specific failures
 * that do not fit into the more specialized exception categories. It supports
 * multiple HTTP status codes through factory methods, each corresponding to
 * a distinct failure scenario: API errors (500), timeouts (504), rate limiting
 * (429), and resolved conversation attempts (410).</p>
 *
 * <p>Each factory method pairs a specific {@link ErrorCode} with an appropriate
 * HTTP status code and a user-friendly detail message. The exception extends
 * {@code ShoppiqException} for integration with the global exception handler
 * and structured error response generation.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Getter
public class AiAssistantException extends ShoppiqException {

    /**
     * Constructs a new {@code AiAssistantException} with the specified error code, HTTP status, and detail.
     *
     * @param errorCode  the machine-readable error code for structured error handling
     * @param httpStatus the HTTP status to return in the response
     * @param detail     a human-readable error description suitable for display to end users
     */
    public AiAssistantException(ErrorCode errorCode, HttpStatus httpStatus, String detail) {
        super(errorCode, httpStatus, detail);
    }

    /**
     * Creates an exception for a general AI API failure.
     *
     * <p>This factory method produces an exception mapped to HTTP 500
     * Internal Server Error, suitable for unexpected failures in the
     * NVIDIA NIM API or LangChain4j processing pipeline.</p>
     *
     * @param detail a human-readable description of the failure
     * @return a new {@code AiAssistantException}
     */
    public static AiAssistantException apiError(String detail) {
        return new AiAssistantException(ErrorCode.AI_API_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR, detail);
    }

    /**
     * Creates an exception for an NIM API timeout.
     *
     * <p>This factory method produces an exception mapped to HTTP 504
     * Gateway Timeout, suitable for cases where the NVIDIA NIM API
     * fails to respond within the configured timeout period.</p>
     *
     * @param detail a human-readable description of the timeout
     * @return a new {@code AiAssistantException}
     */
    public static AiAssistantException timeout(String detail) {
        return new AiAssistantException(ErrorCode.AI_TIMEOUT,
                HttpStatus.GATEWAY_TIMEOUT, detail);
    }

    /**
     * Creates an exception for a rate-limited request.
     *
     * <p>This factory method produces an exception mapped to HTTP 429
     * Too Many Requests, suitable for cases where the client has exceeded
     * the allowed request frequency for the AI chat API.</p>
     *
     * @param detail a human-readable description of the rate limit
     * @return a new {@code AiAssistantException}
     */
    public static AiAssistantException rateLimited(String detail) {
        return new AiAssistantException(ErrorCode.AI_RATE_LIMITED,
                HttpStatus.TOO_MANY_REQUESTS, detail);
    }

    /**
     * Creates an exception for a message sent to an already-resolved conversation.
     *
     * <p>This factory method produces an exception mapped to HTTP 410 Gone,
     * indicating that the conversation has been closed and no further
     * messages can be accepted. The client should start a new conversation
     * instead.</p>
     *
     * @return a new {@code AiAssistantException}
     */
    public static AiAssistantException conversationResolved() {
        return new AiAssistantException(ErrorCode.AI_CONVERSATION_RESOLVED,
                HttpStatus.GONE,
                "This conversation has been resolved. Please start a new conversation.");
    }
}
