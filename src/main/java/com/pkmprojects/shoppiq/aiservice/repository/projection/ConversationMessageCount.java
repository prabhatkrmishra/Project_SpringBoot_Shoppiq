package com.pkmprojects.shoppiq.aiservice.repository.projection;

/**
 * <strong>Spring Boot Concept:</strong> Typed projection for batch-counting messages of a specific role
 * across multiple conversations.
 *
 * <p><b>How AI fits:</b> Used by both {@code ChatServiceImpl} and
 * {@code AdminAiChatServiceImpl} to efficiently compute message counts
 * for conversation summaries without N+1 queries.</p>
 *
 * <p><b>Pattern used:</b> Spring Data JPA projection interface —
 * replaces fragile {@code Object[]} index-based access with
 * a compile-time safe interface.</p>
 *
 * @author prabhatkrmishra
 * @since 1.5.0
 */
public interface ConversationMessageCount {

    /**
     * The conversation identifier.
     *
     * @return conversation ID
     */
    Long getConversationId();

    /**
     * The count of matching messages in this conversation.
     *
     * @return message count
     */
    Long getCount();
}
