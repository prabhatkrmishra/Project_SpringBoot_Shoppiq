package com.pkmprojects.shoppiq.exception.general.review;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a requested
 * item review cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) for missing
 * {@link com.pkmprojects.shoppiq.entity.review.ItemReview} entities.</p>
 *
 * @author prabhatkrmishra
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
