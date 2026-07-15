package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a stock conflict occurs during checkout due to concurrent modifications.
 *
 * <p>This happens when two or more customers attempt to purchase the same item
 * at the same time, and the inventory is depleted between the stock check
 * and the order placement.</p>
 *
 * @author Shoppiq
 * @since 1.0.0
 */
public final class StockConflictException extends InvalidOperationException {

    StockConflictException(String detail) {
        super(ErrorCode.ITEM_STOCK_CONFLICT, detail);
    }

    /**
     * Creates a stock conflict exception for a specific SKU.
     *
     * @param sku the product SKU that experienced the conflict
     * @return exception with user-friendly message
     */
    public static StockConflictException forItem(String sku) {
        return new StockConflictException(
                "Stock conflict for SKU '%s'. Please refresh the page and try again.".formatted(sku)
        );
    }

    /**
     * Creates a stock conflict exception for concurrent optimistic lock failures.
     *
     * @param detail the error message
     * @return exception with the provided detail
     */
    public static StockConflictException forOptimisticLock(String detail) {
        return new StockConflictException(detail);
    }
}