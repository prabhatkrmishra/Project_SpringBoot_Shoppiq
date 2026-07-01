package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a stock adjustment would result in negative inventory.
 *
 * <p>
 * This occurs when attempting to remove more stock than is currently available
 * for a given item.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class ItemStockNegativeException extends InvalidOperationException {

    private ItemStockNegativeException(String detail) {
        super(ErrorCode.ITEM_STOCK_NEGATIVE, detail);
    }

    public static ItemStockNegativeException forAdjustment(String itemName, String sku, int current, int adjustment) {
        return new ItemStockNegativeException(
                "Stock quantity cannot be negative for item '%s' (SKU: %s). Current: %d, Adjustment: %d."
                        .formatted(itemName, sku, current, adjustment)
        );
    }

    public static ItemStockNegativeException forAdjustment(int current, int adjustment) {
        return new ItemStockNegativeException(
                "Stock quantity cannot be negative. Current: %d, Adjustment: %d."
                        .formatted(current, adjustment)
        );
    }
}
