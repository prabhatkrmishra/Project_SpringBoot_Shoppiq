package com.pkmprojects.shoppiq.exception.general.item;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when item details
 * for an Item cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) for the case where the parent
 * {@link com.pkmprojects.shoppiq.entity.item.Item} exists but its associated
 * {@link com.pkmprojects.shoppiq.entity.item.ItemDetails} record is missing.</p>
 *
 * @author prabhatkrmishra
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
