package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when details for an Item cannot be found.
 *
 * <p>
 * This exception indicates that the Item exists but its associated
 * details record could not be located.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class ItemDetailsNotFoundException extends ResourceNotFoundException {

    /**
     * Creates a new ItemDetailsNotFoundException.
     *
     * @param detail detailed error description
     */
    public ItemDetailsNotFoundException(String detail) {
        super(ErrorCode.ITEM_DETAILS_NOT_FOUND, detail);
    }
}