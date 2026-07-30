package com.pkmprojects.shoppiq.aiservice.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when the AI service is disabled or unavailable.
 *
 * <p>This exception is raised when a chat endpoint is invoked but the AI
 * service is not enabled in the application configuration, or when the
 * underlying AI infrastructure (NVIDIA NIM API, Qdrant vector store) is
 * unreachable. It maps to {@link ErrorCode#AI_SERVICE_UNAVAILABLE} and
 * produces an HTTP 503 Service Unavailable response, signaling to the
 * client that the service is temporarily non-functional.</p>
 *
 * <p>The static {@link #disabled()} factory method provides a standardized
 * error message for the common case where the AI feature flag is turned
 * off.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Getter
public class AiServiceUnavailableException extends ShoppiqException {

    public AiServiceUnavailableException(String detail) {
        super(ErrorCode.AI_SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE, detail);
    }

    /**
     * Creates an exception indicating that the AI service is disabled.
     *
     * <p>Returns a standardized error message suitable for display to end
     * users when the AI feature is not available in the current deployment.</p>
     *
     * @return a new {@code AiServiceUnavailableException}
     */
    public static AiServiceUnavailableException disabled() {
        return new AiServiceUnavailableException(
                "AI service is not available. Please try again later.");
    }
}
