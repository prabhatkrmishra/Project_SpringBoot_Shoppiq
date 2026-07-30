package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request DTO for admin stock adjustment operations on a product.
 *
 * <p>This record is submitted to the admin stock adjustment endpoint
 * to manually set the inventory level for a specific product. It is
 * used in scenarios such as receiving a new shipment from a supplier,
 * performing a physical inventory count reconciliation, or writing
 * off damaged or expired stock. The {@code quantity} field represents
 * the absolute replacement value, not a delta from the current
 * stock level.</p>
 *
 * <p>The {@code reason} field is required and provides an audit trail
 * for inventory changes. It is stored alongside the adjustment
 * record and is visible in the stock adjustment history. Common
 * reasons include "New Shipment", "Physical Count", "Damage
 * Write-off", and "Supplier Return".</p>
 *
 * @param quantity the new absolute stock quantity to set; must be
 *                 zero or non-negative; this is not a relative delta
 *                 but a replacement for the current value
 * @param reason   human-readable explanation for the adjustment,
 *                 required, max 255 characters; stored in the audit
 *                 trail for accountability and reporting
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record StockAdjustmentRequest(

        /**
         * New stock quantity (absolute replacement, not delta).
         */
        @NotNull(message = "Quantity is required.")
        @PositiveOrZero(message = "Quantity must be zero or positive.")
        int quantity,

        /**
         * Reason for the adjustment (e.g., "New Shipment", "Physical Count", "Damage Write-off").
         */
        @NotBlank(message = "Reason is required.")
        @jakarta.validation.constraints.Size(max = 255, message = "Reason cannot exceed 255 characters.")
        String reason
) {
}
