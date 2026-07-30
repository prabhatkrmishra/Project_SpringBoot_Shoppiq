package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for previewing an order summary before placing it.
 *
 * <p>This record is submitted to {@code POST /api/orders/calculate} when
 * the frontend needs to display a cost breakdown before the customer
 * confirms checkout. The server calculates all cost components
 * (subtotal, delivery charge, COD surcharge, discount, grand total)
 * from the user's current cart contents, ensuring the frontend never
 * hardcodes or independently calculates prices.</p>
 *
 * <p>The calculation is idempotent and does not create an order; it
 * merely computes what the totals would be if the order were placed
 * with the given parameters. This enables real-time cost updates as
 * the customer toggles delivery type or enters a promo code.</p>
 *
 * @param paymentMethod payment method for cost calculation (CREDIT_CARD,
 *                      UPI, COD); determines whether COD surcharge applies
 * @param deliveryType  delivery type for shipping cost calculation
 *                      (NORMAL, EXPRESS_1DAY); NORMAL is free, EXPRESS_1DAY
 *                      incurs a delivery charge
 * @param promoCode     optional promo code for discount calculation;
 *                      null if no promo code is being applied
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record OrderCalculationRequest(

        /**
         * Payment method for cost calculation.
         */
        @NotNull(message = "Payment method is required.")
        PaymentMethod paymentMethod,

        /**
         * Delivery type for shipping cost calculation.
         */
        @NotNull(message = "Delivery type is required.")
        DeliveryType deliveryType,

        /**
         * Optional promo code for discount calculation.
         */
        String promoCode
) {
}
