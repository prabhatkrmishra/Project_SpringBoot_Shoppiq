package com.pkmprojects.shoppiq.dto.promo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request payload for validating a promo code against cart contents.
 *
 * <p>This record is submitted to {@code POST /api/promo/validate} when
 * a customer enters a promo code during checkout. The server validates
 * the code against the provided cart contents and returns the discount
 * that would be applied. This enables the frontend to display a discount
 * preview before the customer confirms the order.</p>
 *
 * <p>The {@code cartItems} list uses cascading validation via
 * {@code List<@Valid CartItemPreview>} so that each cart item is
 * validated independently. The {@code subtotal} is provided by the
 * frontend as a cross-check against the server's own cart calculation,
 * helping detect potential client-side pricing discrepancies.</p>
 *
 * @param code      promo code string to validate, required; matched
 *                  case-insensitively against active promo codes
 * @param subtotal  current cart subtotal before promo discount, required;
 *                  must be at least 0.01; used for minimum order amount
 *                  validation
 * @param cartItems list of cart line items for coupon-type and quantity
 *                  validation, required, must not be empty; each element
 *                  is validated recursively via {@link CartItemPreview}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PromoCodeValidateRequest(

        /**
         * Promo code string to validate. Must not be blank.
         */
        @NotBlank(message = "Promo code is required.")
        String code,

        /**
         * Current cart subtotal (pre-promo). Must be at least 0.01.
         */
        @DecimalMin(value = "0.01", message = "Subtotal must be at least 0.01.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal subtotal,

        /**
         * Cart line items for coupon-type and quantity validation. Must not be empty.
         */
        @NotEmpty(message = "Cart items are required for promo code validation.")
        List<@Valid CartItemPreview> cartItems
) {
}
