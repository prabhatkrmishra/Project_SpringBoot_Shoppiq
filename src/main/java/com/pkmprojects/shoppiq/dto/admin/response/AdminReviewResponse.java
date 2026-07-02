package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.ReviewStatus;

import java.time.Instant;

/**
 * Response DTO for admin review moderation.
 *
 * <p>
 * This DTO provides a view of a product review for administrators,
 * including reviewer details, product context, and review content.
 * Supports moderation actions (approve, delete).
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose review details to admin API.</li>
 *     <li>Support review moderation operations.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Created using {@link #fromEntity(ItemReview)}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AdminReviewResponse(

        /**
         * Review identifier.
         */
        Long id,

        /**
         * Product name.
         */
        String itemName,

        /**
         * Product SKU.
         */
        String itemSku,

        /**
         * Reviewer username.
         */
        String reviewerUsername,

        /**
         * Reviewer email.
         */
        String reviewerEmail,

        /**
         * Rating (1-5).
         */
        int rating,

        /**
         * Review content.
         */
        String review,

        /**
         * Moderation status of the review.
         */
        ReviewStatus status,

        /**
         * Review creation timestamp.
         */
        Instant createdAt,

        /**
         * Review last update timestamp.
         */
        Instant updatedAt
) {

    /**
     * Creates an {@code AdminReviewResponse} from an {@link ItemReview} entity.
     *
     * @param review review entity
     * @return mapped response DTO
     */
    public static AdminReviewResponse fromEntity(ItemReview review) {
        Item item = review.getItem();
        User reviewer = review.getUser();
        return new AdminReviewResponse(
                review.getId(),
                item.getName(),
                item.getItemDetails().getSku(),
                reviewer.getUsername(),
                reviewer.getEmail(),
                review.getRating(),
                review.getReview(),
                review.getStatus(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}