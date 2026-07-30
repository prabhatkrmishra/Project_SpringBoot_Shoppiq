package com.pkmprojects.shoppiq.dto.order;

import java.math.BigDecimal;

/**
 * Server-calculated order cost summary returned before the order is placed.
 *
 * <p>This record provides a complete cost breakdown computed by the server
 * from the user's current cart contents and the selected checkout
 * parameters. Every cost component is calculated server-side to ensure
 * the frontend displays authoritative, consistent values regardless of
 * client-side implementation.</p>
 *
 * <p>The grand total is computed as
 * {@code subtotal + deliveryCharge + codSurcharge - discount}. This
 * response is returned by the order calculation endpoint and is used
 * by the checkout page to render the order summary before the customer
 * confirms the purchase.</p>
 *
 * @param subtotal       sum of all item line totals before any fees or discounts;
 *                       computed from the user's current cart contents
 * @param deliveryCharge shipping fee: {@code 7.50} for EXPRESS_1DAY,
 *                       {@code 0.00} for NORMAL delivery
 * @param codSurcharge   additional surcharge: {@code 5.00} when payment
 *                       method is COD, {@code 0.00} otherwise
 * @param discount       promo code discount amount; {@code 0.00} if no valid
 *                       promo code is applied
 * @param grandTotal     final amount the customer pays, computed as
 *                       {@code subtotal + deliveryCharge + codSurcharge - discount}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record OrderCalculationResponse(

        /** Sum of all item line totals before any fees or discounts. */
        BigDecimal subtotal,

        /** Delivery charge: {@code 7.50} for EXPRESS_1DAY, {@code 0} for NORMAL. */
        BigDecimal deliveryCharge,

        /** COD surcharge: {@code 5.00} when COD, {@code 0} otherwise. */
        BigDecimal codSurcharge,

        /** Promo code discount amount. */
        BigDecimal discount,

        /** Final total the customer pays (subtotal + deliveryCharge + codSurcharge - discount). */
        BigDecimal grandTotal
) {
}
