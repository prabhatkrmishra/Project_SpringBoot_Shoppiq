package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.ItemReview;
import com.pkmprojects.shoppiq.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository responsible for {@link ItemReview} persistence.
 *
 * <p>
 * Provides CRUD operations together with lookup methods required by
 * the product review module.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Persist product reviews.</li>
 *     <li>Retrieve reviews by item.</li>
 *     <li>Retrieve reviews by reviewer.</li>
 *     <li>Enforce one review per user per item.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * Returns the 10 most recent reviews.
     *
     * @return list of recent reviews
     */
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
}