package com.pkmprojects.shoppiq.dto.promo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request payload for validating a promo code against cart contents.
 *
 * @param code      the promo code string to validate
 * @param subtotal  the current cart subtotal (pre-promo)
 * @param cartItems cart line items for coupon-type and quantity validation
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record PromoCodeValidateRequest(

        @NotBlank(message = "Promo code is required.")
        String code,

        @DecimalMin(value = "0.01", message = "Subtotal must be at least 0.01.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal subtotal,

        @NotEmpty(message = "Cart items are required for promo code validation.")
        List<@Valid CartItemPreview> cartItems
) {
}
