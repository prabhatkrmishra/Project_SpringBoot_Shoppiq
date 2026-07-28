package com.pkmprojects.shoppiq.dto.promo;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Represents a single cart line item for promo code validation preview.
 *
 * <p>Used as a nested element inside {@link PromoCodeValidateRequest}. The
 * frontend sends its current cart items so the server can validate quantity-based
 * coupon constraints (e.g., SINGLE vs BULK coupon types).</p>
 *
 * <p><b>Validation:</b> {@code @Positive} for quantity (must be ≥ 1),
 * {@code @DecimalMin("0.01")} + {@code @Digits} for unit price (must be a
 * positive monetary value).</p>
 *
 * @param itemDetailsId the item details (variant) ID
 * @param quantity      units of this item in the cart
 * @param unitPrice     effective unit price (post-item-discount)
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public record CartItemPreview(
        @NotNull(message = "Item details ID is required.")
        Long itemDetailsId,

        @Positive(message = "Quantity must be at least 1.")
        int quantity,

        @DecimalMin(value = "0.01", message = "Unit price must be positive.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal unitPrice
) {
}
