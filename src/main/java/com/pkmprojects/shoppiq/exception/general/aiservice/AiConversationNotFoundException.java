package com.pkmprojects.shoppiq.exception.general.aiservice;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when an AI chat conversation cannot be found.
 *
 * <p>This exception is thrown by AI service methods when a database
 * lookup for a conversation fails. It uses the
 * {@link ErrorCode#AI_CONVERSATION_NOT_FOUND} code and HTTP 404 Not
 * Found status. The conversation may have been deleted or the ID may
 * be incorrect.</p>
 *
 * <p>The detail message includes the conversation identifier (e.g.,
 * "AI conversation with chatId 'abc-123' was not found.") to help the
 * client understand which conversation was invalid. The client should
 * verify the conversation ID and retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#AI_CONVERSATION_NOT_FOUND
 * @since 1.0.0
 */
public final class AiConversationNotFoundException extends ResourceNotFoundException {

    private AiConversationNotFoundException(String detail) {
        super(ErrorCode.AI_CONVERSATION_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a conversation not found by its public chat ID.
     *
     * @param chatId the public chat identifier that was not found
     * @return a new {@code AiConversationNotFoundException}
     */
    public static AiConversationNotFoundException chatId(String chatId) {
        return new AiConversationNotFoundException(
                "AI conversation with chatId '%s' was not found.".formatted(chatId)
        );
    }

    /**
     * Creates an exception for a conversation not found by its database ID.
     *
     * @param id the database primary key that was not found
     * @return a new {@code AiConversationNotFoundException}
     */
    public static AiConversationNotFoundException id(Long id) {
        return new AiConversationNotFoundException(
                "AI conversation with id '%d' was not found.".formatted(id)
        );
    }
}
