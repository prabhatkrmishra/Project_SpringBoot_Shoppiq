package com.pkmprojects.shoppiq.aiservice.repository.projection;

/**
 * Projection for batch-counting messages of a specific role across conversations.
 *
 * <p>This lightweight interface is used by Spring Data JPA to return aggregated
 * message count data without loading full entity graphs. It supports the admin
 * dashboard's conversation listing by providing pre-computed message counts
 * in a single query, avoiding N+1 performance issues.</p>
 *
 * @author prabhatkrmishra
 * @since 1.5.0
 */
public interface ConversationMessageCount {

    /**
     * The conversation identifier.
     *
     * <p>This corresponds to the internal database ID of the conversation,
     * used as the key in the batch-count result map.</p>
     *
     * @return conversation ID
     */
    Long getConversationId();

    /**
     * The count of matching messages in this conversation.
     *
     * <p>Represents the number of messages with the specified role
     * (typically {@code USER}) within the conversation.</p>
     *
     * @return message count
     */
    Long getCount();
}
