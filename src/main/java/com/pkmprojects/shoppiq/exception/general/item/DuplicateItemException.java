package com.pkmprojects.shoppiq.exception.general.item;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when attempting to create or update an item whose SKU already exists.
 *
 * <p>This exception is thrown during product creation or update when the
 * submitted SKU conflicts with an existing item. It uses the
 * {@link ErrorCode#ITEM_ALREADY_EXISTS} code and HTTP 409 Conflict
 * status. The exception provides a static factory method for SKU-specific
 * conflicts and a generic method for database constraint violations where
 * the exact field is unknown.</p>
 *
 * <p>The detail message includes the conflicting SKU (e.g., "Item with
 * SKU 'PROD-001' already exists.") to help the client understand which
 * identifier caused the conflict. The client should use a different SKU
 * or update the existing item instead.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ITEM_ALREADY_EXISTS
 * @since 1.0.0
 */
public final class DuplicateItemException
        extends DuplicateResourceException {

    /**
     * Creates a duplicate item exception.
     *
     * @param detail detailed description
     */
    private DuplicateItemException(String detail) {
        super(ErrorCode.ITEM_ALREADY_EXISTS, detail);
    }

    /**
     * Creates an exception indicating that the supplied SKU
     * already exists.
     *
     * @param sku duplicate SKU
     * @return duplicate item exception
     */
    public static DuplicateItemException sku(String sku) {
        return new DuplicateItemException(
                "Item with SKU '%s' already exists."
                        .formatted(sku)
        );
    }

    /**
     * Creates a generic duplicate item exception.
     *
     * @return duplicate item exception
     */
    public static DuplicateItemException unknown() {
        return new DuplicateItemException(
                "An item with the supplied information already exists."
        );
    }
}
