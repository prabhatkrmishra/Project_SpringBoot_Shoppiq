package com.pkmprojects.shoppiq.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;



/**
 * Request DTO used for creating and updating an item review.
 *
 * <p>
 * This DTO represents the customer supplied review information
 * submitted through the REST API.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Accept customer review data.</li>
 *     <li>Validate rating constraints.</li>
 *     <li>Remain independent of persistence entities.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>The reviewed item is identified by the request URL path, not the body.</li>
 *     <li>The reviewer is determined from the authenticated user (security context).</li>
 *     <li>This DTO contains only mutable review data (rating + text).</li>
 *     <li>This DTO is a Java record and inherits {@code Serializable}
 *     implicitly; the explicit declaration was removed (BUG-0031).</li>
 * </ul>
 *
 * <h2>Validation Rules</h2>
 * <ul>
 *     <li>{@code @Min(1)} / {@code @Max(5)} — rating must be between 1 and 5.</li>
 *     <li>Review text is optional (no {@code @NotBlank}).</li>
 *     <li>{@code @Size(max = 1000)} — review text length cap.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ItemReviewRequest(

        /*
          Rating assigned to the product.
         */
        @NotNull(message = "Rating is required.")
        @Min(value = 1, message = "Rating must be at least 1.")
        @Max(value = 5, message = "Rating cannot exceed 5.")
        Integer rating,

        /*
          Optional written review.
         */
        @Size(
                max = 1000,
                message = "Review cannot exceed 1000 characters."
        )
        String review

) {
}
