package com.pkmprojects.shoppiq.aiservice.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a user attempts to access an AI conversation they do not own.
 *
 * <p>
 * Maps to {@link ErrorCode#AI_ACCESS_DENIED} and produces a 403 Forbidden response.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Getter
public class AiAccessDeniedException extends ShoppiqException {

    public AiAccessDeniedException(String detail) {
        super(ErrorCode.AI_ACCESS_DENIED, HttpStatus.FORBIDDEN, detail);
    }

    /**
     * Creates an exception for a user attempting to access another user's conversation.
     *
     * @param chatId the conversation chat ID that was accessed without authorization
     * @return a new {@code AiAccessDeniedException}
     */
    public static AiAccessDeniedException forConversation(String chatId) {
        return new AiAccessDeniedException(
                "You do not have access to conversation '%s'.".formatted(chatId));
    }
}
