package com.pkmprojects.shoppiq.aiservice.repository;

import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.aiservice.service.ChatServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for the {@link ChatConversation} aggregate.
 *
 * <p>This Spring Data JPA repository provides CRUD operations and custom
 * query methods for AI chat conversations. It supports conversation lookup
 * by public chat ID, ownership validation, status filtering, free-text
 * search across multiple fields, and time-based queries for the auto-resolution
 * scheduled task.</p>
 *
 * <p>The repository includes custom JPQL queries for admin dashboard search
 * functionality, supporting case-insensitive matching across chat ID, title,
 * and username fields with optional status filtering.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    /**
     * Finds a conversation by its public-facing chat ID.
     *
     * <p>Used for conversation lookup in all chat operations. The chat ID
     * is the primary identifier exposed to the frontend and is used for
     * ownership validation, message retrieval, and conversation resolution.</p>
     *
     * @param chatId the public chat identifier (e.g., {@code CHAT-2026-07-A3F2})
     * @return the conversation, or {@link Optional#empty()} if not found
     */
    Optional<ChatConversation> findByChatId(String chatId);

    /**
     * Checks whether a conversation with the given chat ID already exists.
     *
     * <p>Used by the chat ID generation loop in {@link ChatServiceImpl} to
     * ensure uniqueness when creating new conversations.</p>
     *
     * @param chatId the public chat identifier to check
     * @return {@code true} if a conversation with this ID exists
     */
    boolean existsByChatId(String chatId);

    /**
     * Returns all conversations for a given user, ordered by most recently updated.
     *
     * <p>Used by the conversation list endpoint to display the user's sidebar
     * conversation list. Results are sorted by {@code updatedAt} descending
     * to show the most recently active conversations first.</p>
     *
     * @param userId the ID of the conversation owner
     * @return list of conversations sorted by {@code updatedAt} descending
     */
    List<ChatConversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    /**
     * Returns conversations for a user filtered by status.
     *
     * <p>Used for filtered conversation views, allowing users to see only
     * active or only resolved conversations.</p>
     *
     * @param userId the ID of the conversation owner
     * @param status the desired conversation status
     * @return list of matching conversations sorted by {@code updatedAt} descending
     */
    List<ChatConversation> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, ConversationStatus status);

    /**
     * Returns all conversations associated with a guest session identifier.
     *
     * <p>Used for guest conversation lookup by session UUID. Guest conversations
     * are tracked by the {@code GUEST_SESSION} cookie value rather than user
     * identity.</p>
     *
     * @param guestSession the guest session UUID
     * @return list of conversations sorted by {@code updatedAt} descending
     */
    List<ChatConversation> findByGuestSessionOrderByUpdatedAtDesc(String guestSession);

    /**
     * Returns a paginated view of all conversations, ordered by most recently updated.
     *
     * <p>Used by the admin dashboard for the default conversation listing when
     * no search or status filter is applied.</p>
     *
     * @param pageable pagination parameters
     * @return paginated list of conversations
     */
    Page<ChatConversation> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    /**
     * Returns a paginated view of conversations filtered by status.
     *
     * <p>Used by the admin dashboard when filtering conversations by their
     * lifecycle status (ACTIVE or RESOLVED).</p>
     *
     * @param status   the desired conversation status
     * @param pageable pagination parameters
     * @return paginated list of matching conversations
     */
    Page<ChatConversation> findByStatusOrderByUpdatedAtDesc(ConversationStatus status, Pageable pageable);

    /**
     * Searches conversations by a free-text query across chat ID, title, and username.
     *
     * <p>Performs case-insensitive {@code LIKE} matching against the chat ID,
     * conversation title, and associated user's username. Used by the admin
     * dashboard for the conversation search feature. The query uses JPQL
     * with {@code LOWER()} and {@code CONCAT()} for database-portable
     * case-insensitive matching.</p>
     *
     * @param query    the search term to match against
     * @param pageable pagination parameters
     * @return paginated list of matching conversations
     */
    @Query("""
            SELECT c FROM ChatConversation c
            WHERE LOWER(c.chatId) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.user.username) LIKE LOWER(CONCAT('%', :query, '%'))""")
    Page<ChatConversation> searchByQuery(@Param("query") String query, Pageable pageable);

    /**
     * Searches conversations by a free-text query, filtered by status.
     *
     * <p>Combines the free-text search with a status filter, narrowing results
     * to conversations that match the search term AND have the specified status.
     * Used by the admin dashboard when both search and status filters are active.</p>
     *
     * @param query    the search term to match against
     * @param status   the desired conversation status
     * @param pageable pagination parameters
     * @return paginated list of matching conversations
     */
    @Query("""
            SELECT c FROM ChatConversation c
            WHERE c.status = :status
              AND (LOWER(c.chatId) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(c.user.username) LIKE LOWER(CONCAT('%', :query, '%')))""")
    Page<ChatConversation> searchByQueryAndStatus(@Param("query") String query,
                                                  @Param("status") ConversationStatus status,
                                                  Pageable pageable);

    /**
     * Finds all conversations with the given status that were last updated before
     * the specified cutoff time.
     *
     * <p>Used by the auto-resolve scheduled task in {@link ChatServiceImpl} to
     * find inactive conversations that should be resolved. The cutoff is
     * typically set to 30 minutes before the current time, identifying
     * conversations that have had no activity in that window.</p>
     *
     * @param status the desired conversation status (typically {@link ConversationStatus#ACTIVE})
     * @param cutoff the cutoff timestamp — conversations updated before this time are considered inactive
     * @return list of inactive conversations matching the criteria
     */
    List<ChatConversation> findByStatusAndUpdatedAtBefore(ConversationStatus status, Instant cutoff);
}
