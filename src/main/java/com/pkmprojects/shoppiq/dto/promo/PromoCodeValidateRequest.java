package com.pkmprojects.shoppiq.dto.promo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Request payload for validating a promo code against an order subtotal.
 *
 * @param code     the promo code string to validate
 * @param subtotal the current cart/order subtotal to calculate the discount against
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record PromoCodeValidateRequest(

        @NotBlank(message = "Promo code is required.")
        String code,

        @DecimalMin(value = "0.01", message = "Subtotal must be at least 0.01.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal subtotal
) {
}
