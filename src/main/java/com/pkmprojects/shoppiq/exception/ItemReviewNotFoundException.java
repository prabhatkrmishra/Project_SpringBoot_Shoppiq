package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when an Item review cannot be found.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class ItemReviewNotFoundException extends ResourceNotFoundException {

    /**
     * Creates a new ItemReviewNotFoundException.
     *
     * @param detail detailed error description
     */
    public ItemReviewNotFoundException(String detail) {
        super(ErrorCode.ITEM_REVIEW_NOT_FOUND, detail);
    }
}