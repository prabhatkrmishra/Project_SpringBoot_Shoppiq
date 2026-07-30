package com.pkmprojects.shoppiq.aiservice.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a request references an AI model that is not in the allowed registry.
 *
 * <p>This exception is raised when the frontend specifies a model identifier
 * that does not match any entry in the {@code ModelResolutionService}
 * registry. It maps to {@link ErrorCode#AI_MODEL_NOT_SUPPORTED} and
 * produces an HTTP 400 Bad Request response, allowing the client to
 * correct the model selection. The exception message includes the
 * unsupported model ID for debugging purposes.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Getter
public class AiModelNotSupportedException extends ShoppiqException {

    public AiModelNotSupportedException(String detail) {
        super(ErrorCode.AI_MODEL_NOT_SUPPORTED, HttpStatus.BAD_REQUEST, detail);
    }

    /**
     * Creates an exception for a model ID that is not in the allowed registry.
     *
     * <p>The returned exception includes the unsupported model ID in its
     * detail message, enabling the frontend to display a specific error
     * and suggest valid alternatives.</p>
     *
     * @param modelId the model identifier that was requested
     * @return a new {@code AiModelNotSupportedException}
     */
    public static AiModelNotSupportedException forModel(String modelId) {
        return new AiModelNotSupportedException(
                "AI model '%s' is not supported. Please select a different model.".formatted(modelId));
    }
}
