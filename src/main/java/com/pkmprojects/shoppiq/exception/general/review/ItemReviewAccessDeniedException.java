package com.pkmprojects.shoppiq.exception.general.review;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a user attempts to update or delete a review they do not own.
 *
 * <p>This exception is thrown when a user attempts to modify or delete a
 * review that belongs to another user. Only the review author or an
 * administrator can modify a review. It uses the
 * {@link ErrorCode#ITEM_REVIEW_ACCESS_DENIED} code and HTTP 403 Forbidden
 * status.</p>
 *
 * <p>The detail message includes the review identifier (e.g.,
 * "You are not allowed to modify review with id '42'.") to help the
 * client understand which review was restricted. The client should ensure
 * they are operating on their own reviews.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ITEM_REVIEW_ACCESS_DENIED
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
