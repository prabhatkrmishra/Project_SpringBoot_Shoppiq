package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a user attempts to update or delete a review
 * they do not own and is not an administrator.
 *
 * @author PrabhatKrMishra
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
}
