package com.pkmprojects.shoppiq.dto.response;

import com.pkmprojects.shoppiq.entity.ItemReview;

import java.time.Instant;

/**
 * Response DTO returned for {@link ItemReview} resources.
 *
 * <p>
 * This DTO represents the publicly visible information of a product review.
 * It exposes review information together with basic reviewer details while
 * hiding internal persistence implementation.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose review information.</li>
 *     <li>Expose reviewer information.</li>
 *     <li>Hide JPA entities from the REST API.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Created using {@link #fromEntity(ItemReview)}.</li>
 *     <li>Does not expose the complete {@code User} entity.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ItemReviewResponse(

        /*
          Review identifier.
         */
        Long id,

        /*
          Reviewer identifier.
         */
        Long reviewerId,

        /*
          Reviewer's display name.
         */
        String reviewerName,

        /*
          Reviewer's username.
         */
        String reviewerUsername,

        /*
          Rating assigned by the reviewer.
         */
        Integer rating,

        /*
          Written review.
         */
        String review,

        /*
          Creation timestamp.
         */
        Instant createdAt,

        /*
          Last modification timestamp.
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
                review.getUser().getId(),
                review.getUser().getName(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getReview(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}