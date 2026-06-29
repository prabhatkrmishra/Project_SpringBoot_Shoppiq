package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when the requested cart quantity exceeds available stock.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class InsufficientStockException extends InvalidOperationException {

    private InsufficientStockException(String detail) {
        super(ErrorCode.INSUFFICIENT_STOCK, detail);
    }

    public static InsufficientStockException forItem(String sku, int requested, int available) {
        return new InsufficientStockException(
                "Insufficient stock for SKU '%s': requested %d, available %d."
                        .formatted(sku, requested, available)
        );
    }
}
