package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when an AI chat conversation cannot be found by its chat ID or database ID.
 *
 * <p>
 * This is a concrete subclass of {@link ResourceNotFoundException} used exclusively
 * by the AI service layer. It maps to {@link ErrorCode#AI_CONVERSATION_NOT_FOUND}
 * and produces a 404 HTTP response.
 *
 * @author PrabhatKrMishra
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
