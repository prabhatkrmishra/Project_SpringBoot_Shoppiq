package com.pkmprojects.shoppiq.exception.general.item;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when item details (specifications, descriptions) for a product cannot be found.
 *
 * <p>This exception is thrown by item service methods when a database
 * lookup for detailed product information fails. Item details are created
 * separately from the item itself and may not always be present. It uses
 * the {@link ErrorCode#ITEM_DETAILS_NOT_FOUND} code and HTTP 404 Not
 * Found status.</p>
 *
 * <p>The detail message includes the item details identifier (e.g.,
 * "Item details with id '42' were not found.") to help the client
 * understand which details were missing. The client should verify the
 * identifier and retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ITEM_DETAILS_NOT_FOUND
 * @since 1.0.0
 */
public final class ItemDetailsNotFoundException extends ResourceNotFoundException {

    /**
     * Creates a new ItemDetailsNotFoundException.
     *
     * @param detail detailed error description
     */
    private ItemDetailsNotFoundException(String detail) {
        super(ErrorCode.ITEM_DETAILS_NOT_FOUND, detail);
    }

    /**
     * Creates an exception indicating that item details with the given ID were not found.
     *
     * @param id the item details ID
     * @return item details not found exception
     */
    public static ItemDetailsNotFoundException id(Long id) {
        return new ItemDetailsNotFoundException(
                "Item details with id '%d' were not found.".formatted(id)
        );
    }
}
