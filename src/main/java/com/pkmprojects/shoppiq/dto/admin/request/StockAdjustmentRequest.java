package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for admin stock adjustment operations.
 *
 * <p>
 * Used by administrators to manually adjust product stock levels
 * (e.g., after receiving a new shipment, performing a physical count,
 * or correcting data entry errors).
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Accept stock adjustment requests from admin API.</li>
 *     <li>Validate adjustment parameters.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Positive quantity indicates stock addition.</li>
 *     <li>Reason field provides audit trail context.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record StockAdjustmentRequest(

        /**
         * Quantity to adjust (positive for addition, negative for reduction).
         */
        @NotNull(message = "Quantity is required.")
        @Positive(message = "Quantity must be positive.")
        int quantity,

        /**
         * Reason for the adjustment (e.g., "New Shipment", "Physical Count", "Damage Write-off").
         */
        @NotBlank(message = "Reason is required.")
        @jakarta.validation.constraints.Size(max = 255, message = "Reason cannot exceed 255 characters.")
        String reason
) {
}