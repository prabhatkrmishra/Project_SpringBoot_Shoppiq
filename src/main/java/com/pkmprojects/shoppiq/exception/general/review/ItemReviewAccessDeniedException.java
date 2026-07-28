package com.pkmprojects.shoppiq.exception.general.review;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a user attempts
 * to update or delete a review they do not own.
 *
 * <p>Leaf exception in the authorization hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException}
 * (HTTP 403) with factory methods for ownership violations and role-based
 * restrictions (sellers and admins cannot create reviews).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class ItemReviewAccessDeniedException
        extends UnauthorizedOperationException {

    /**
     * Creates an item review access denied exception.
     *
     * @param detail detailed error description
     */
    private ItemReviewAccessDeniedException(String detail) {
        super(ErrorCode.ITEM_REVIEW_ACCESS_DENIED, detail);
    }

    /**
     * Creates an exception indicating that the supplied user does not
     * own the review identified by {@code reviewId} and is not an admin.
     *
     * @param reviewId review identifier
     * @return item review access denied exception
     */
    public static ItemReviewAccessDeniedException forReview(Long reviewId) {
        return new ItemReviewAccessDeniedException(
                "You are not allowed to modify review with id '%d'."
                        .formatted(reviewId)
        );
    }

    /**
     * Creates an exception for a seller attempting to create a review.
     *
     * @return a new exception instance
     */
    public static ItemReviewAccessDeniedException sellerCannotReview() {
        return new ItemReviewAccessDeniedException(
                "Sellers are not allowed to create reviews."
        );
    }

    /**
     * Creates an exception for an admin attempting to create a review.
     *
     * @return a new exception instance
     */
    public static ItemReviewAccessDeniedException adminCannotReview() {
        return new ItemReviewAccessDeniedException(
                "Admins are not allowed to create reviews."
        );
    }
}
