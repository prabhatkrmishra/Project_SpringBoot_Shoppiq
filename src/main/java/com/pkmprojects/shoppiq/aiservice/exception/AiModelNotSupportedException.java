package com.pkmprojects.shoppiq.aiservice.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a request references an AI model that is not in the allowed registry.
 *
 * <p>
 * Maps to {@link ErrorCode#AI_MODEL_NOT_SUPPORTED} and produces a 400 Bad Request response.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Getter
public class AiModelNotSupportedException extends ShoppiqException {

    public AiModelNotSupportedException(String detail) {
        super(ErrorCode.AI_MODEL_NOT_SUPPORTED, HttpStatus.BAD_REQUEST, detail);
    }

    /**
     * Creates an exception for an unsupported model ID.
     *
     * @param modelId the model identifier that was requested
     * @return a new {@code AiModelNotSupportedException}
     */
    public static AiModelNotSupportedException forModel(String modelId) {
        return new AiModelNotSupportedException(
                "AI model '%s' is not supported. Please select a different model.".formatted(modelId));
    }
}
