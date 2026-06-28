package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a requested item review cannot be found.
 *
 * <p>
 * This exception represents lookup failures for {@code ItemReview} resources.
 * It is typically thrown by the service layer when a review cannot be
 * resolved using its identifier.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represents missing item review resources.</li>
 *     <li>Associates the failure with
 *     {@link ErrorCode#ITEM_REVIEW_NOT_FOUND}.</li>
 *     <li>Provides expressive factory methods for common lookup failures.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>The constructor is private to enforce factory method usage.</li>
 *     <li>Factory methods centralize message creation.</li>
 *     <li>Additional lookup scenarios can be added without modifying callers.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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