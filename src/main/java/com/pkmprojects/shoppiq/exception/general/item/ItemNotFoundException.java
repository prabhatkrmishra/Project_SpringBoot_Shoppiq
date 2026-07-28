package com.pkmprojects.shoppiq.exception.general.item;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a requested
 * item cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) for missing items, with factory methods for lookup by
 * identifier, SKU, and slug.</p>
 *
 * @author prabhatkrmishra
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
