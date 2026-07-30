package com.pkmprojects.shoppiq.exception.general.inventory;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when the requested cart quantity exceeds available stock.
 *
 * <p>This exception is thrown when a customer attempts to add more units
 * of an item to their cart than are currently in stock. The system
 * prevents over-commitment of inventory to maintain data integrity and
 * accurate stock counts. It uses the {@link ErrorCode#INSUFFICIENT_STOCK}
 * code and HTTP 400 Bad Request status.</p>
 *
 * <p>The detail message includes the SKU, requested quantity, and
 * available quantity (e.g., "Insufficient stock for SKU 'PROD-001':
 * requested 5, available 3.") to help the client understand how many
 * units are actually available.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#INSUFFICIENT_STOCK
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
