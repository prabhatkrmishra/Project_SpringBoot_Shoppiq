package com.pkmprojects.shoppiq.exception.general.inventory;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a stock
 * adjustment would result in negative inventory.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to enforce the data integrity rule that inventory cannot go
 * negative.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class ItemStockNegativeException extends InvalidOperationException {

    private ItemStockNegativeException(String detail) {
        super(ErrorCode.ITEM_STOCK_NEGATIVE, detail);
    }

    /**
     * Creates an exception for a negative stock adjustment with item details.
     *
     * @param itemName  the item name
     * @param sku       the item SKU
     * @param current   the current stock level
     * @param adjustment the attempted adjustment
     * @return a new exception instance
     */
    public static ItemStockNegativeException forAdjustment(String itemName, String sku, int current, int adjustment) {
        return new ItemStockNegativeException(
                "Stock quantity cannot be negative for item '%s' (SKU: %s). Current: %d, Adjustment: %d."
                        .formatted(itemName, sku, current, adjustment)
        );
    }

    /**
     * Creates an exception for a generic negative stock adjustment.
     *
     * @param current   the current stock level
     * @param adjustment the attempted adjustment
     * @return a new exception instance
     */
    public static ItemStockNegativeException forAdjustment(int current, int adjustment) {
        return new ItemStockNegativeException(
                "Stock quantity cannot be negative. Current: %d, Adjustment: %d."
                        .formatted(current, adjustment)
        );
    }
}
