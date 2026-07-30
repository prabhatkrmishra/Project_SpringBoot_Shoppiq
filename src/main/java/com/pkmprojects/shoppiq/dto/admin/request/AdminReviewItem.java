package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inner item DTO for bulk review creation by an administrator.
 *
 * <p>This record specifies a single product review to be created on
 * behalf of a specific user. It is used as an element within
 * {@link BulkReviewRequest} for administrative test-data population,
 * enabling administrators to populate the review system with sample
 * data in a single API call. Reviews created via this DTO are
 * automatically set to PENDING moderation status at the service
 * layer.</p>
 *
 * <p>The {@code rating} is constrained to the 1-5 integer range
 * using Jakarta Bean Validation. The {@code review} text is optional
 * and capped at 1000 characters, allowing administrators to create
 * both rating-only reviews and detailed written reviews.</p>
 *
 * @param userId identifier of the existing user who will be recorded
 *               as the review author; must reference a valid {@code User} entity
 * @param itemId identifier of the product being reviewed; must
 *               reference an existing {@code Item} entity
 * @param rating integer rating from 1 (lowest) to 5 (highest);
 *               required, cannot be null
 * @param review optional written review text, max 1000 characters;
 *               may be null for rating-only reviews
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminReviewItem(
        /**
         * ID of the existing user who will own the review.
         */
        @NotNull(message = "User ID is required.")
        Long userId,

        /**
         * ID of the item being reviewed.
         */
        @NotNull(message = "Item ID is required.")
        Long itemId,

        /**
         * Rating value. Must be between 1 and 5.
         */
        @NotNull(message = "Rating is required.")
        @Min(value = 1, message = "Rating must be at least 1.")
        @Max(value = 5, message = "Rating cannot exceed 5.")
        Integer rating,

        /**
         * Optional written review text. Maximum 1000 characters.
         */
        @Size(max = 1000, message = "Review cannot exceed 1000 characters.")
        String review
) {
}
