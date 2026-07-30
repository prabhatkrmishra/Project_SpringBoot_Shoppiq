package com.pkmprojects.shoppiq.exception.general.review;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested item review cannot be found.
 *
 * <p>This exception is thrown by review service methods when a database
 * lookup for a review fails. It uses the
 * {@link ErrorCode#ITEM_REVIEW_NOT_FOUND} code and HTTP 404 Not Found
 * status. The review may have been removed by the author or by an
 * administrator.</p>
 *
 * <p>The detail message includes the review identifier (e.g.,
 * "Item review with id '42' was not found.") to help the client
 * understand which review was invalid. The client should verify the
 * review ID and retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ITEM_REVIEW_NOT_FOUND
 * @since 1.0.0
 */
public final class ItemReviewNotFoundException
        extends ResourceNotFoundException {

    /**
     * Creates an item review not found exception.
     *
     * @param detail detailed error description
     */
    private ItemReviewNotFoundException(String detail) {
        super(ErrorCode.ITEM_REVIEW_NOT_FOUND, detail);
    }

    /**
     * Creates an exception indicating that no review exists with the
     * supplied identifier.
     *
     * @param id review identifier
     * @return item review not found exception
     */
    public static ItemReviewNotFoundException id(Long id) {
        return new ItemReviewNotFoundException(
                "Item review with id '%d' was not found."
                        .formatted(id)
        );
    }
}
