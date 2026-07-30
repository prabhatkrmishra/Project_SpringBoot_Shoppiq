package com.pkmprojects.shoppiq.aiservice.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a user attempts to access an AI conversation they do not own.
 *
 * <p>This exception is raised during ownership validation when a user tries
 * to send messages to, retrieve messages from, or resolve a conversation
 * that belongs to another user. It maps to {@link ErrorCode#AI_ACCESS_DENIED}
 * and produces an HTTP 403 Forbidden response, preventing unauthorized
 * access to private conversation data.</p>
 *
 * <p>The static {@link #forConversation(String)} factory method includes the
 * contested chat ID in the error message for debugging and audit logging
 * purposes.</p>
 *
 * @author prabhatkrmishra
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
     * <p>The returned exception includes the contested chat ID in its detail
     * message, enabling the frontend to display a specific access denied
     * error and supporting audit trail generation.</p>
     *
     * @param chatId the conversation chat ID that was accessed without authorization
     * @return a new {@code AiAccessDeniedException}
     */
    public static AiAccessDeniedException forConversation(String chatId) {
        return new AiAccessDeniedException(
                "You do not have access to conversation '%s'.".formatted(chatId));
    }
}
