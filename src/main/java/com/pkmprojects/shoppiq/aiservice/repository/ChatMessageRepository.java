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
     * detail view. Messages are ordered by {@code id} (monotonic
     * auto-increment) ascending to reconstruct the full conversation thread.</p>
     *
     * <p>Ordering by {@code id} is deliberate: the {@code created_at} column is
     * a second-precision {@code TIMESTAMP}, so messages persisted within the
     * same second share a timestamp. A filesort over equal keys is not
     * guaranteed to be stable in MySQL, which can reorder messages (e.g., a
     * SYSTEM "resolved" message ending up before the assistant reply it follows).
     * The auto-incrementing {@code id} is a deterministic, strictly monotonic
     * proxy for insertion order.</p>
     *
     * @param conversationId the parent conversation's ID
     * @return list of messages ordered by {@code id} ascending
     */
    List<ChatMessage> findByConversationIdOrderByIdAsc(Long conversationId);

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
     * Finds the most recent message with the given role that was persisted
     * strictly before the given message id.
     *
     * <p>Used by the auto-resolution heuristic in {@link ChatServiceImpl} to
     * check whether the assistant message that immediately preceded the user's
     * closing reply was a closing prompt (e.g., "Is there anything else I can
     * help you with?"). Ordering by {@code id} (monotonic auto-increment) is
     * deterministic, unlike {@code created_at} which is stored at second
     * precision and can tie within the same second.</p>
     *
     * @param conversationId the conversation's internal ID
     * @param role           the message role to filter by (typically ASSISTANT)
     * @param beforeId       the id of the user message that follows the target message
     * @return the most recent matching message persisted before {@code beforeId}, if any
     */
    Optional<ChatMessage> findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(Long conversationId,
                                                                                     ChatMessageRole role,
                                                                                     Long beforeId);
}
