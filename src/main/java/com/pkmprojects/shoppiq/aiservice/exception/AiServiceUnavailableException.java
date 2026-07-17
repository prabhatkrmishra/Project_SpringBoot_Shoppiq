package com.pkmprojects.shoppiq.aiservice.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when the AI service is disabled or unavailable.
 *
 * <p>
 * Maps to {@link ErrorCode#AI_SERVICE_UNAVAILABLE} and produces a 503 Service Unavailable response.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Getter
public class AiServiceUnavailableException extends ShoppiqException {

    public AiServiceUnavailableException(String detail) {
        super(ErrorCode.AI_SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE, detail);
    }

    /**
     * Creates an exception for a disabled AI service.
     *
     * @return a new {@code AiServiceUnavailableException}
     */
    public static AiServiceUnavailableException disabled() {
        return new AiServiceUnavailableException(
                "AI service is not available. Please try again later.");
    }
}
