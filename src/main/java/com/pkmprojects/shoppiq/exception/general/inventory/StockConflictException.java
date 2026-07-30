package com.pkmprojects.shoppiq.exception.general.inventory;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a stock conflict occurs during checkout due to concurrent modifications.
 *
 * <p>This exception is thrown when an optimistic locking conflict is
 * detected during the checkout process. Another customer or process has
 * modified the stock since it was last read. It uses the
 * {@link ErrorCode#ITEM_STOCK_CONFLICT} code and HTTP 409 Conflict
 * status.</p>
 *
 * <p>The detail message advises the client to refresh the cart and retry
 * the checkout (e.g., "Stock conflict for SKU 'PROD-001'. Please refresh
 * the page and try again."). This is a transient error that typically
 * resolves on retry.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ITEM_STOCK_CONFLICT
 * @since 1.0.0
 */
public final class StockConflictException extends InvalidOperationException {

    private StockConflictException(String detail) {
        super(ErrorCode.ITEM_STOCK_CONFLICT, detail);
    }

    /**
     * Creates an exception for a stock conflict on a specific SKU.
     *
     * @param sku the SKU that experienced the conflict
     * @return a new exception instance
     */
    public static StockConflictException forItem(String sku) {
        return new StockConflictException(
                "Stock conflict for SKU '%s'. Please refresh the page and try again.".formatted(sku)
        );
    }

    /**
     * Creates an exception for a generic optimistic lock failure.
     *
     * @param detail the error message
     * @return a new exception instance
     */
    public static StockConflictException forOptimisticLock(String detail) {
        return new StockConflictException(detail);
    }
}
