package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for placing an order at checkout.
 *
 * <p>This record is submitted to {@code POST /api/orders/checkout} when
 * a customer confirms their order. It carries the shipping address
 * reference, payment method, delivery speed selection, and an optional
 * promo code. The service layer uses the user's current cart contents
 * to construct the order, applying all pricing calculations server-side
 * to ensure accuracy.</p>
 *
 * <p>The {@code paymentMethod} and {@code deliveryType} fields accept
 * enum values that Spring Boot automatically deserializes from JSON
 * strings. Invalid enum values result in a descriptive error response.
 * The {@code promoCode} is nullable; when omitted, no discount is
 * applied to the order.</p>
 *
 * @param addressId     identifier of the shipping address to use for this
 *                      order; must reference an existing address in the
 *                      user's address book
 * @param paymentMethod payment method for the order (CREDIT_CARD, UPI,
 *                      COD); determines applicable surcharges and
 *                      payment gateway routing
 * @param deliveryType  delivery speed selection (NORMAL, EXPRESS_1DAY);
 *                      affects delivery charge calculation and fulfillment
 *                      priority
 * @param promoCode     optional promo code string for discount application;
 *                      null if no promo code is being used; validated and
 *                      applied at the service layer
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CheckoutRequest(

        /**
         * ID of the shipping address to use for this order.
         */
        @NotNull(message = "Address ID is required.")
        Long addressId,

        /**
         * Payment method (e.g., CREDIT_CARD, UPI, COD).
         */
        @NotNull(message = "Payment method is required.")
        PaymentMethod paymentMethod,

        /**
         * Delivery speed (NORMAL, EXPRESS_1DAY).
         */
        @NotNull(message = "Delivery type is required.")
        DeliveryType deliveryType,

        /**
         * Optional promo code. Null if none.
         */
        String promoCode
) {
}
