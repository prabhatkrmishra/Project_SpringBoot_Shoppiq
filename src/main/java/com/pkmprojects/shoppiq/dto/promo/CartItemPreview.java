package com.pkmprojects.shoppiq.dto.promo;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Represents a single cart line item for promo code validation preview.
 *
 * @param itemDetailsId the item details (variant) ID
 * @param quantity      units of this item in the cart
 * @param unitPrice     effective unit price (post-item-discount)
 * @author PrabhatKrMishra
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
