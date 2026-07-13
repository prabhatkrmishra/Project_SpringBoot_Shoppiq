package com.pkmprojects.shoppiq.aiservice.repository;

import com.pkmprojects.shoppiq.aiservice.entity.ChatMessage;
import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link ChatMessage} persistence.
 *
 * <p>
 * Provides message retrieval by conversation, role-based counting for
 * conversation summaries, and latest-message lookups for activity tracking.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Returns all messages for a conversation in chronological order.
     *
     * @param conversationId the parent conversation's ID
     * @return list of messages ordered by {@code createdAt} ascending
     */
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /**
     * Counts messages of a specific role within a conversation.
     *
     * <p>
     * Primarily used to count {@link ChatMessageRole#USER} messages for the
     * conversation summary's message count display.
     *
     * @param conversationId the parent conversation's ID
     * @param role           the message role to count
     * @return the number of matching messages
     */
    long countByConversationIdAndRole(Long conversationId, ChatMessageRole role);

    /**
     * Batch-counts user messages across multiple conversations in a single query.
     *
     * @param conversationIds the list of conversation IDs to count for
     * @return a list of [conversationId, count] pairs
     */
    @Query("SELECT m.conversation.id, COUNT(m) FROM ChatMessage m " +
            "WHERE m.conversation.id IN :ids AND m.role = :role " +
            "GROUP BY m.conversation.id")
    List<Object[]> countByConversationIdsAndRoleBatch(@Param("ids") List<Long> conversationIds,
                                                      @Param("role") ChatMessageRole role);

    /**
     * Returns the most recent message of a specific role within a conversation.
     *
     * @param conversationId the parent conversation's ID
     * @param role           the desired message role
     * @return the latest message, or {@code null} if none exists
     */
    ChatMessage findFirstByConversationIdAndRoleOrderByCreatedAtDesc(Long conversationId, ChatMessageRole role);

    /**
     * Deletes all messages belonging to a conversation.
     *
     * @param conversationId the parent conversation's ID
     */
    void deleteByConversationId(Long conversationId);
}
