package com.pkmprojects.shoppiq.aiservice.repository;

import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
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
 * Spring Data repository for {@link ChatConversation} persistence.
 *
 * <p>
 * Provides both single-result lookups and paginated queries. The search methods
 * ({@link #searchByQuery}, {@link #searchByQueryAndStatus}) perform case-insensitive
 * {@code LIKE} matching across chat ID, title, and username — used by the admin
 * dashboard for conversation filtering.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    /**
     * Finds a conversation by its public-facing chat ID.
     *
     * @param chatId the public chat identifier (e.g., {@code CHAT-2026-07-A3F2})
     * @return the conversation, or {@link Optional#empty()} if not found
     */
    Optional<ChatConversation> findByChatId(String chatId);

    /**
     * Checks whether a conversation with the given chat ID already exists.
     *
     * @param chatId the public chat identifier to check
     * @return {@code true} if a conversation with this ID exists
     */
    boolean existsByChatId(String chatId);

    /**
     * Returns all conversations for a given user, ordered by most recently updated.
     *
     * @param userId the ID of the conversation owner
     * @return list of conversations sorted by {@code updatedAt} descending
     */
    List<ChatConversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    /**
     * Returns conversations for a user filtered by status.
     *
     * @param userId the ID of the conversation owner
     * @param status the desired conversation status
     * @return list of matching conversations sorted by {@code updatedAt} descending
     */
    List<ChatConversation> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, ConversationStatus status);

    /**
     * Returns all conversations associated with a guest session identifier.
     *
     * @param guestSession the guest session UUID
     * @return list of conversations sorted by {@code updatedAt} descending
     */
    List<ChatConversation> findByGuestSessionOrderByUpdatedAtDesc(String guestSession);

    /**
     * Returns a paginated view of all conversations, ordered by most recently updated.
     *
     * @param pageable pagination parameters
     * @return paginated list of conversations
     */
    Page<ChatConversation> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    /**
     * Returns a paginated view of conversations filtered by status.
     *
     * @param status   the desired conversation status
     * @param pageable pagination parameters
     * @return paginated list of matching conversations
     */
    Page<ChatConversation> findByStatusOrderByUpdatedAtDesc(ConversationStatus status, Pageable pageable);

    /**
     * Searches conversations by a free-text query across chat ID, title, and username.
     *
     * <p>
     * Performs case-insensitive {@code LIKE} matching. Used by the admin dashboard
     * for the conversation search feature.
     *
     * @param query    the search term to match against
     * @param pageable pagination parameters
     * @return paginated list of matching conversations
     */
    @Query("SELECT c FROM ChatConversation c WHERE " +
            "LOWER(c.chatId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.user.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ChatConversation> searchByQuery(@Param("query") String query, Pageable pageable);

    /**
     * Searches conversations by a free-text query, filtered by status.
     *
     * @param query    the search term to match against
     * @param status   the desired conversation status
     * @param pageable pagination parameters
     * @return paginated list of matching conversations
     */
    @Query("SELECT c FROM ChatConversation c WHERE c.status = :status AND " +
            "(LOWER(c.chatId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.user.username) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ChatConversation> searchByQueryAndStatus(@Param("query") String query,
                                                  @Param("status") ConversationStatus status,
                                                  Pageable pageable);

    /**
     * Finds all conversations with the given status that were last updated before the specified cutoff time.
     *
     * <p>
     * Used by the auto-resolve scheduled task to find inactive conversations that should be resolved.
     *
     * @param status the desired conversation status (typically {@link ConversationStatus#ACTIVE})
     * @param cutoff the cutoff timestamp — conversations updated before this time are considered inactive
     * @return list of inactive conversations matching the criteria
     */
    List<ChatConversation> findByStatusAndUpdatedAtBefore(ConversationStatus status, Instant cutoff);
}
