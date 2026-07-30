package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.ReviewStatus;

import java.time.Instant;

/**
 * Response DTO for admin review moderation management.
 *
 * <p>This record provides a full view of a product review for
 * administrators, including reviewer identity, product context,
 * review content, and moderation status. It is returned by the
 * admin review list and detail endpoints and is designed for the
 * review moderation UI where administrators approve, reject, or
 * delete customer-submitted reviews.</p>
 *
 * <p>The static {@link #fromEntity(ItemReview)} factory method
 * traverses the {@code ItemReview → Item, User} entity graph to
 * flatten the data into a single DTO. The {@code status} field
 * tracks the review's position in the moderation workflow
 * (PENDING, APPROVED, REJECTED) and controls whether the review
 * is visible on the public product page.</p>
 *
 * @param id               unique identifier of the review
 * @param itemName         name of the product being reviewed, for context
 * @param itemSku          SKU of the product being reviewed, for precise identification
 * @param reviewerUsername username of the customer who submitted the review
 * @param reviewerEmail    email address of the reviewer for admin follow-up
 * @param rating           integer rating from 1 (lowest) to 5 (highest) assigned
 *                         by the reviewer
 * @param review           optional written review text submitted by the customer;
 *                         may be null for rating-only reviews
 * @param status           moderation status controlling visibility
 *                         (PENDING, APPROVED, REJECTED)
 * @param createdAt        timestamp when the review was first submitted
 * @param updatedAt        timestamp of the most recent modification to the review
 * @author prabhatkrmishra
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
