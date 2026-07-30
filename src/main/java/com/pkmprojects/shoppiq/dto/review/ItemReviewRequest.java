package com.pkmprojects.shoppiq.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * Request DTO for creating or updating a product review.
 *
 * <p>This record carries the customer-supplied review data submitted
 * through the REST API. It contains only the mutable review fields
 * (rating and optional text); the reviewed item is identified by the
 * request URL path, and the reviewer is determined from the
 * authenticated user's security context.</p>
 *
 * <p>The rating is constrained to the 1-5 integer range using Jakarta
 * Bean Validation. The review text is optional, allowing customers to
 * submit rating-only reviews. When provided, the text is capped at
 * 1000 characters to prevent excessively long submissions. Reviews
 * are created with PENDING moderation status and become visible on
 * the product page only after admin approval.</p>
 *
 * @param rating integer rating from 1 (lowest) to 5 (highest);
 *               required, cannot be null
 * @param review optional written review text, max 1000 characters;
 *               may be null for rating-only reviews
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ItemReviewRequest(

        /**
         * Rating assigned to the product. Must be between 1 and 5.
         */
        @NotNull(message = "Rating is required.")
        @Min(value = 1, message = "Rating must be at least 1.")
        @Max(value = 5, message = "Rating cannot exceed 5.")
        Integer rating,

        /**
         * Optional written review. Maximum 1000 characters.
         */
        @Size(
                max = 1000,
                message = "Review cannot exceed 1000 characters."
        )
        String review

) {
}
