package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.ItemReview;
import org.springframework.data.jpa.repository.JpaRepository;

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
}