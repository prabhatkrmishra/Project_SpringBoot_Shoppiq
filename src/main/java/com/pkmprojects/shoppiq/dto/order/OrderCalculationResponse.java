package com.pkmprojects.shoppiq.dto.order;

import java.math.BigDecimal;

/**
 * Server-calculated order summary returned before the order is placed.
 *
 * <p>Every cost component — including delivery charge and COD surcharge — is
 * computed server-side so the frontend displays authoritative values.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record OrderCalculationResponse(

        /** Sum of all item line totals before any fees or discounts. */
        BigDecimal subtotal,

        /** Delivery charge: {@code 7.50} for EXPRESS_1DAY, {@code 0} for NORMAL. */
        BigDecimal deliveryCharge,

        /** COD surcharge: {@code 5.00} when COD, {@code 0} otherwise. */
        BigDecimal codSurcharge,

        /** Total shipping fee (deliveryCharge + codSurcharge). */
        BigDecimal shippingFee,

        /** Promo code discount amount. */
        BigDecimal discount,

        /** Final total the customer pays (subtotal + shippingFee - discount). */
        BigDecimal grandTotal
) {
}
