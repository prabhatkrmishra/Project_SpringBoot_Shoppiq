package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when attempting to create or update an item whose
 * unique business identifier already exists.
 *
 * <p>
 * Currently the uniqueness constraint is enforced on the SKU.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represents duplicate item resources.</li>
 *     <li>Associates the failure with
 *     {@link ErrorCode#ITEM_ALREADY_EXISTS}.</li>
 *     <li>Provides expressive factory methods.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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