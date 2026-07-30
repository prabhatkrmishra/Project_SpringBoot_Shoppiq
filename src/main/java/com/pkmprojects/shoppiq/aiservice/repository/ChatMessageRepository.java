package com.pkmprojects.shoppiq.aiservice.repository;

import com.pkmprojects.shoppiq.aiservice.entity.ChatMessage;
import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.aiservice.service.ChatServiceImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for the {@link ChatMessage} aggregate.
 *
 * <p>This Spring Data JPA repository provides CRUD operations and custom
 * query methods for AI chat messages. It supports chronological message
 * retrieval, role-based message counting, batch counting across multiple
 * conversations, and targeted lookups for auto-resolution heuristics.</p>
 *
 * <p>The repository includes both standard Spring Data derived queries
 * and custom JPQL for batch operations that avoid N+1 query issues in
 * the admin dashboard and conversation summary endpoints.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Returns all messages for a conversation in chronological order.
     *
     * <p>Used by the message history endpoint and the admin conversation
     * detail view. Messages are ordered by {@code createdAt} ascending
     * to reconstruct the full conversation thread.</p>
     *
     * @param conversationId the parent conversation's ID
     * @return list of messages ordered by {@code createdAt} ascending
     */
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /**
     * Counts messages of a specific role within a conversation.
     *
     * <p>Primarily used to count {@link ChatMessageRole#USER} messages for
     * the conversation summary's message count display and for the
     * auto-resolution threshold check.</p>
     *
     * @param conversationId the parent conversation's ID
     * @param role           the message role to count
     * @return the number of matching messages
     */
    long countByConversationIdAndRole(Long conversationId, ChatMessageRole role);

    /**
     * Batch-counts user messages across multiple conversations in a single query.
     *
     * <p>Used by the admin dashboard's conversation listing to populate the
     * message count field without N+1 query issues. Returns a list of
     * {@code [conversationId, count]} pairs that are mapped into a lookup
     * map by the calling service.</p>
     *
     * @param conversationIds the list of conversation IDs to count for
     * @param role            the message role to count
     * @return a list of [conversationId, count] pairs
     */
    @Query("""
            SELECT m.conversation.id, COUNT(m) FROM ChatMessage m
            WHERE m.conversation.id IN :ids AND m.role = :role
            GROUP BY m.conversation.id""")
    List<Object[]> countByConversationIdsAndRoleBatch(@Param("ids") List<Long> conversationIds,
                                                      @Param("role") ChatMessageRole role);

    /**
     * Returns the most recent message of a specific role within a conversation.
     *
     * <p>Used for various lookup operations including retrieving the last
     * assistant message for auto-resolution heuristics. Returns null if
     * no message of the specified role exists in the conversation.</p>
     *
     * @param conversationId the parent conversation's ID
     * @param role           the desired message role
     * @return the latest message, or {@code null} if none exists
     */
    ChatMessage findFirstByConversationIdAndRoleOrderByCreatedAtDesc(Long conversationId, ChatMessageRole role);

    /**
     * Deletes all messages belonging to a conversation.
     *
     * <p>Used by the admin conversation deletion operation to perform a
     * cascading delete of all child messages before removing the parent
     * conversation entity.</p>
     *
     * @param conversationId the parent conversation's ID
     */
    void deleteByConversationId(Long conversationId);

    /**
     * Finds the most recently created message with the given role for a conversation.
     *
     * <p>Used by the auto-resolution heuristic in {@link ChatServiceImpl} to
     * check whether the assistant's last message was a closing prompt (e.g.,
     * "Is there anything else I can help you with?"). This check prevents
     * false auto-resolves when the user's short reply was answering some other
     * assistant question rather than confirming they're finished.</p>
     *
     * @param conversationId the conversation's internal ID
     * @param role           the message role to filter by (typically ASSISTANT)
     * @return the most recent matching message, if any
     */
    Optional<ChatMessage> findTopByConversationIdAndRoleOrderByCreatedAtDesc(Long conversationId, ChatMessageRole role);
}
