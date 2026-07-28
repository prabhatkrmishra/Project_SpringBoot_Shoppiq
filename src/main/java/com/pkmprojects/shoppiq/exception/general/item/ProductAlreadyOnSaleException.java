package com.pkmprojects.shoppiq.exception.general.item;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when an attempt
 * is made to put a product on sale that is already on sale.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to prevent redundant sale status changes.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class ProductAlreadyOnSaleException extends InvalidOperationException {

    private ProductAlreadyOnSaleException(String detail) {
        super(ErrorCode.ITEM_ALREADY_ON_SALE, detail);
    }

    /**
     * Creates an exception for an item that is already on sale.
     *
     * @param itemName the item name
     * @return a new exception instance
     */
    public static ProductAlreadyOnSaleException forItem(String itemName) {
        return new ProductAlreadyOnSaleException(
                "Product '%s' is already on sale."
                        .formatted(itemName)
        );
    }
}
