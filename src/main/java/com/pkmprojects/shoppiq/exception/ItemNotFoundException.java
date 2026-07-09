package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a requested item cannot be found.
 *
 * <p>
 * This exception represents lookup failures for {@code Item} resources.
 * It is typically thrown by the service layer when an item cannot be
 * resolved using its identifier or SKU.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represents missing item resources.</li>
 *     <li>Associates the failure with
 *     {@link ErrorCode#ITEM_NOT_FOUND}.</li>
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