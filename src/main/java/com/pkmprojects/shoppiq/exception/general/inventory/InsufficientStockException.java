package com.pkmprojects.shoppiq.exception.general.inventory;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when the requested
 * cart quantity exceeds available stock.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) for inventory constraint violations during checkout.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class InsufficientStockException extends InvalidOperationException {

    private InsufficientStockException(String detail) {
        super(ErrorCode.INSUFFICIENT_STOCK, detail);
    }

    /**
     * Creates an exception for insufficient stock for a specific item.
     *
     * @param sku       the item SKU
     * @param requested the quantity requested
     * @param available the quantity available
     * @return a new exception instance
     */
    public static InsufficientStockException forItem(String sku, int requested, int available) {
        return new InsufficientStockException(
                "Insufficient stock for SKU '%s': requested %d, available %d."
                        .formatted(sku, requested, available)
        );
    }
}
