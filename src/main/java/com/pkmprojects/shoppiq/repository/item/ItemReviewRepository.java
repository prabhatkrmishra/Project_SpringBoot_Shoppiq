package com.pkmprojects.shoppiq.repository.item;

import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository responsible for {@link ItemReview} persistence.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived ordering by nested property</strong> — {@code findAllByItemIdOrderByCreatedAtDesc}
 *       generates {@code SELECT * FROM item_reviews WHERE item_id = ? ORDER BY created_at DESC}.</li>
 *   <li><strong>Combined field query for uniqueness</strong> — {@code findByUserIdAndItemId}
 *       and {@code existsByUserIdAndItemId} enforce the one-review-per-user-per-item business rule.</li>
 *   <li><strong>{@code Top} / {@code Limit}</strong> — {@code findTop10ByOrderByCreatedAtDesc}
 *       generates {@code SELECT * FROM item_reviews ORDER BY created_at DESC LIMIT 10}.</li>
 *   <li><strong>{@code @EntityGraph}</strong> — Eagerly fetches {@code item} and {@code user}
 *       associations to prevent N+1 queries for the recent reviews listing.</li>
 *   <li><strong>Complex JPQL with OR conditions</strong> — {@code findVisibleReviewsForUser}
 *       uses a custom {@code @Query} with boolean logic:
 *       {@code WHERE item.id = ? AND (status = 'APPROVED' OR (user.id = ? AND status = 'PENDING'))}.</li>
 *   <li><strong>Derived query with enum parameter</strong> — {@code findAllByItemIdAndStatusOrderByCreatedAtDesc}
 *       demonstrates filtering by both foreign key and enum field.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findAllByItemIdOrderByCreatedAtDesc(Long)
 *       → SELECT * FROM item_reviews WHERE item_id = ? ORDER BY created_at DESC
 *   findAllByUserIdOrderByCreatedAtDesc(Long)
 *       → SELECT * FROM item_reviews WHERE user_id = ? ORDER BY created_at DESC
 *   findByUserIdAndItemId(Long, Long)
 *       → SELECT * FROM item_reviews WHERE user_id = ? AND item_id = ?
 *   existsByUserIdAndItemId(Long, Long)
 *       → SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
 *         FROM item_reviews WHERE user_id = ? AND item_id = ?
 *   findTop10ByOrderByCreatedAtDesc
 *       → SELECT * FROM item_reviews ORDER BY created_at DESC LIMIT 10
 *   findAllByItemIdAndStatusOrderByCreatedAtDesc(Long, ReviewStatus)
 *       → SELECT * FROM item_reviews WHERE item_id = ? AND status = ? ORDER BY created_at DESC
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface ItemReviewRepository
        extends JpaRepository<ItemReview, Long> {

    /**
     * Retrieves every review belonging to an item ordered by
     * newest first.
     *
     * @param itemId item identifier
     * @return ordered review list
     */
    List<ItemReview> findAllByItemIdOrderByCreatedAtDesc(Long itemId);

    /**
     * Retrieves every review written by a user ordered by
     * newest first.
     *
     * @param userId reviewer identifier
     * @return ordered review list
     */
    List<ItemReview> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Page<ItemReview> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Finds a review written by a user for a specific item.
     *
     * @param userId reviewer identifier
     * @param itemId item identifier
     * @return matching review if present
     */
    Optional<ItemReview> findByUserIdAndItemId(
            Long userId,
            Long itemId
    );

    /**
     * Determines whether a user has already reviewed
     * the specified item.
     *
     * @param userId reviewer identifier
     * @param itemId item identifier
     * @return {@code true} if a review already exists
     */
    boolean existsByUserIdAndItemId(
            Long userId,
            Long itemId
    );

    /**
     * Returns the 10 most recent reviews, with item and user eagerly fetched.
     *
     * @return list of recent reviews
     */
    @EntityGraph(attributePaths = {"item", "user"})
    List<ItemReview> findTop10ByOrderByCreatedAtDesc();

    /**
     * Returns APPROVED reviews for an item, plus the specified user's
     * own PENDING reviews (so the creator can see their review awaiting
     * approval). REJECTED reviews are hidden from everyone.
     *
     * @param itemId item identifier
     * @param userId current user identifier (may be null for anonymous)
     * @return ordered review list
     */
    @Query("SELECT r FROM ItemReview r WHERE r.item.id = :itemId AND (" +
            "r.status = com.pkmprojects.shoppiq.enums.ReviewStatus.APPROVED" +
            " OR (r.user.id = :userId AND r.status = com.pkmprojects.shoppiq.enums.ReviewStatus.PENDING))" +
            " ORDER BY r.createdAt DESC")
    List<ItemReview> findVisibleReviewsForUser(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId
    );

    /**
     * Returns APPROVED reviews for an item (public view, no user context).
     *
     * @param itemId item identifier
     * @return ordered review list
     */
    List<ItemReview> findAllByItemIdAndStatusOrderByCreatedAtDesc(
            Long itemId,
            ReviewStatus status
    );

    /**
     * Paginated version of {@link #findVisibleReviewsForUser} (BUG-005).
     */
    @Query("SELECT r FROM ItemReview r WHERE r.item.id = :itemId AND (" +
            "r.status = com.pkmprojects.shoppiq.enums.ReviewStatus.APPROVED" +
            " OR (r.user.id = :userId AND r.status = com.pkmprojects.shoppiq.enums.ReviewStatus.PENDING))" +
            " ORDER BY r.createdAt DESC")
    Page<ItemReview> findVisibleReviewsForUser(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Paginated version of {@link #findAllByItemIdAndStatusOrderByCreatedAtDesc} (BUG-005).
     */
    Page<ItemReview> findAllByItemIdAndStatusOrderByCreatedAtDesc(
            Long itemId,
            ReviewStatus status,
            Pageable pageable
    );
}
