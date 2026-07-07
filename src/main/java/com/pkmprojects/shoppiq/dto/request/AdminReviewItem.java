package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inner item DTO used by {@link BulkReviewRequest} for bulk review creation.
 *
 * <p>
 * Each item specifies the target user, the item being reviewed, and the
 * rating/review content.
 * </p>
 *
 * @param userId ID of the existing user who will own the review
 * @param itemId ID of the item being reviewed
 * @param rating rating value (1–5)
 * @param review optional written review text
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AdminReviewItem(
        @NotNull(message = "User ID is required.")
        Long userId,

        @NotNull(message = "Item ID is required.")
        Long itemId,

        @NotNull(message = "Rating is required.")
        @Min(value = 1, message = "Rating must be at least 1.")
        @Max(value = 5, message = "Rating cannot exceed 5.")
        Integer rating,

        @Size(max = 1000, message = "Review cannot exceed 1000 characters.")
        String review
) {
}
