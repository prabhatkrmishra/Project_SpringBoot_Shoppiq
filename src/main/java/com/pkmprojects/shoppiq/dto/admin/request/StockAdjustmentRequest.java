package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

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
 *     <li>{@code quantity} is an <b>absolute replacement</b>, not a delta — the
 *     service layer sets stock to this exact value.</li>
 *     <li>{@code reason} provides audit trail context (e.g., "New Shipment").</li>
 *     <li>{@code @PositiveOrZero} allows setting stock to zero (out of stock).</li>
 * </ul>
 *
 * <p><b>Why a record?</b> The compact syntax clearly communicates the DTO's
 * shape: exactly two fields with their constraints visible at a glance.</p>
 *
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
