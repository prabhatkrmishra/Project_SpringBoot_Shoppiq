package com.pkmprojects.shoppiq.exception.general.item;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when an attempt is made to put a product on sale that is already on sale.
 *
 * <p>This exception is thrown when an admin attempts to apply a sale price
 * to a product that is already discounted. Only one sale can be active at
 * a time per product. It uses the {@link ErrorCode#ITEM_ALREADY_ON_SALE}
 * code and HTTP 400 Bad Request status.</p>
 *
 * <p>The detail message includes the product name (e.g., "Product
 * 'Laptop' is already on sale.") to help the client understand which
 * product was rejected. The admin should remove the existing sale before
 * applying a new one.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ITEM_ALREADY_ON_SALE
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
