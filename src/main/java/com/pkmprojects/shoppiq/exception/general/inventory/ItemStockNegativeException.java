package com.pkmprojects.shoppiq.exception.general.inventory;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a stock adjustment would result in negative inventory.
 *
 * <p>This exception is thrown when an admin or seller attempts to reduce
 * stock below zero. The system prevents negative inventory to maintain
 * data integrity and accurate stock counts. It uses the
 * {@link ErrorCode#ITEM_STOCK_NEGATIVE} code and HTTP 400 Bad Request
 * status.</p>
 *
 * <p>The detail message includes the item details, current stock, and
 * attempted adjustment (e.g., "Stock quantity cannot be negative for
 * item 'Laptop' (SKU: LAP-001). Current: 3, Adjustment: -5.") to help
 * the client understand why the adjustment was rejected.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ITEM_STOCK_NEGATIVE
 * @since 1.0.0
 */
public final class ItemStockNegativeException extends InvalidOperationException {

    private ItemStockNegativeException(String detail) {
        super(ErrorCode.ITEM_STOCK_NEGATIVE, detail);
    }

    /**
     * Creates an exception for a negative stock adjustment with item details.
     *
     * @param itemName   the item name
     * @param sku        the item SKU
     * @param current    the current stock level
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
     * @param current    the current stock level
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
