package com.pkmprojects.shoppiq.dto.promo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Cart line item representation used for promo code validation preview.
 *
 * <p>This record represents a single cart line item as sent by the
 * frontend during promo code validation. It is used as a nested
 * element within {@link PromoCodeValidateRequest} so the server can
 * validate quantity-based coupon constraints (e.g. SINGLE vs BULK
 * coupon types) against the actual cart contents without needing to
 * query the database for the user's cart.</p>
 *
 * <p>The {@code unitPrice} is the effective price after any item-level
 * discount, which is the price used for promo code discount
 * calculations. The server uses these values to verify minimum order
 * amounts, per-item quantity thresholds, and coupon type constraints.</p>
 *
 * @param itemDetailsId identifier of the specific product variant
 *                      ({@code ItemDetails} record) in the cart
 * @param quantity      number of units of this variant in the cart;
 *                      must be at least 1
 * @param unitPrice     effective unit price after any item-level discount;
 *                      must be a positive monetary value; used as the basis
 *                      for promo code discount calculations
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public record CartItemPreview(
        /**
         * Item details (variant) ID.
         */
        @NotNull(message = "Item details ID is required.")
        Long itemDetailsId,

        /**
         * Units of this item in the cart. Must be at least 1.
         */
        @Positive(message = "Quantity must be at least 1.")
        int quantity,

        /**
         * Effective unit price (post-item-discount). Must be positive.
         */
        @DecimalMin(value = "0.01", message = "Unit price must be positive.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal unitPrice
) {
}
