package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when the requested Item resource cannot be found.
 *
 * <p>
 * This exception is typically thrown by the service layer when an Item
 * with the specified identifier does not exist in the database.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represent a missing Item resource.</li>
 *     <li>Associate the failure with {@link ErrorCode#ITEM_NOT_FOUND}.</li>
 *     <li>Provide expressive factory methods for Item lookup failures.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>The constructor is private to enforce the use of factory methods.</li>
 *     <li>Error message creation is centralized within this class.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class ItemNotFoundException extends ResourceNotFoundException {

    /**
     * Creates a new ItemNotFoundException.
     *
     * @param detail detailed error description
     */
    public ItemNotFoundException(String detail) {
        super(ErrorCode.ITEM_NOT_FOUND, detail);
    }
}