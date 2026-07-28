package com.pkmprojects.shoppiq.exception.general.aiservice;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when an AI chat
 * conversation cannot be found by its chat ID or database ID.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) with multiple factory methods for different lookup scenarios
 * (public chat ID and internal database ID).</p>
 *
 * @author prabhatkrmishra
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
