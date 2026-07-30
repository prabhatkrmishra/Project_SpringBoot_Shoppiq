package com.pkmprojects.shoppiq.exception.general.item;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested item (product) cannot be found by ID, SKU, or slug.
 *
 * <p>This exception is thrown by item service methods when a database
 * lookup for a product fails. It uses the {@link ErrorCode#ITEM_NOT_FOUND}
 * code and HTTP 404 Not Found status. The exception provides multiple
 * static factory methods to create instances for different lookup
 * scenarios, each with a descriptive detail message.</p>
 *
 * <p>The detail message includes the lookup identifier and type (e.g.,
 * "Item with id '42' was not found.") to help clients understand which
 * identifier was invalid. The client should verify the identifier and
 * retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ITEM_NOT_FOUND
 * @since 1.0.0
 */
public final class ItemNotFoundException extends ResourceNotFoundException {

    /**
     * Creates an item not found exception.
     *
     * @param detail detailed error description
     */
    private ItemNotFoundException(String detail) {
        super(ErrorCode.ITEM_NOT_FOUND, detail);
    }

    /**
     * Creates an exception indicating that no item exists with the
     * supplied identifier.
     *
     * @param id item identifier
     * @return item not found exception
     */
    public static ItemNotFoundException id(Long id) {
        return new ItemNotFoundException(
                "Item with id '%d' was not found.".formatted(id)
        );
    }

    /**
     * Creates an exception indicating that no item exists with the
     * supplied SKU.
     *
     * @param sku item SKU
     * @return item not found exception
     */
    public static ItemNotFoundException sku(String sku) {
        return new ItemNotFoundException(
                "Item with SKU '%s' was not found.".formatted(sku)
        );
    }

    /**
     * Creates an exception indicating that no item exists with the
     * supplied slug.
     *
     * @param slug item slug
     * @return item not found exception
     */
    public static ItemNotFoundException slug(String slug) {
        return new ItemNotFoundException(
                "Item with slug '%s' was not found.".formatted(slug)
        );
    }
}
