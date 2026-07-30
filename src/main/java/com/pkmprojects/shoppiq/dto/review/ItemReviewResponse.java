package com.pkmprojects.shoppiq.dto.review;

import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.enums.ReviewStatus;

import java.time.Instant;

/**
 * Response DTO returned for product review resources.
 *
 * <p>This record exposes the publicly visible information of a product
 * review, including the review content, rating, and basic reviewer
 * details. It is returned by the product review list endpoint and is
 * designed for the product detail page where customer reviews are
 * displayed. Only reviews with APPROVED status are visible on the
 * public product page.</p>
 *
 * <p>The DTO does not expose the complete User entity, providing only
 * {@code reviewerId}, {@code reviewerName}, and {@code reviewerUsername}
 * for privacy. The static {@link #fromEntity(ItemReview)} factory
 * method handles the entity-to-DTO mapping, and the {@code status}
 * field tracks the review's moderation state (PENDING, APPROVED,
 * REJECTED).</p>
 *
 * @param id               unique identifier of the review record
 * @param itemId           identifier of the product being reviewed
 * @param itemName         display name of the product for context
 * @param reviewerId       identifier of the reviewer's user account
 * @param reviewerName     display name of the reviewer
 * @param reviewerUsername username of the reviewer for attribution
 * @param rating           integer rating from 1 (lowest) to 5 (highest)
 * @param review           optional written review text; may be null for
 *                         rating-only reviews
 * @param status           moderation status controlling public visibility
 *                         (PENDING, APPROVED, REJECTED)
 * @param createdAt        timestamp when the review was first submitted
 * @param updatedAt        timestamp of the most recent modification
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ItemReviewResponse(

        /**
         * Review identifier.
         */
        Long id,

        /**
         * Item identifier.
         */
        Long itemId,

        /**
         * Item name.
         */
        String itemName,

        /**
         * Reviewer identifier.
         */
        Long reviewerId,

        /**
         * Reviewer's display name.
         */
        String reviewerName,

        /**
         * Reviewer's username.
         */
        String reviewerUsername,

        /**
         * Rating assigned by the reviewer.
         */
        Integer rating,

        /**
         * Written review.
         */
        String review,

        /**
         * Moderation status.
         */
        ReviewStatus status,

        /**
         * Creation timestamp.
         */
        Instant createdAt,

        /**
         * Last modification timestamp.
         */
        Instant updatedAt

) {

    /**
     * Creates an {@code ItemReviewResponse} from an
     * {@link ItemReview} entity.
     *
     * <p>
     * Centralizes mapping between the persistence layer and
     * the REST API.
     * </p>
     *
     * @param review review entity
     * @return mapped response
     */
    public static ItemReviewResponse fromEntity(ItemReview review) {
        return new ItemReviewResponse(
                review.getId(),
                review.getItem().getId(),
                review.getItem().getName(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getReview(),
                review.getStatus(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
