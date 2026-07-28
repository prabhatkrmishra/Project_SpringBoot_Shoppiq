package com.pkmprojects.shoppiq.exception.general.inventory;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a stock
 * conflict occurs during checkout due to concurrent modifications.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to represent optimistic locking failures when two customers
 * purchase the same item simultaneously.</p>
 *
 * @author prabhatkrmishra
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
