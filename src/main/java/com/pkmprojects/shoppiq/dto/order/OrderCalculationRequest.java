package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for previewing an order summary before placing it.
 *
 * <p>The server calculates all cost components (subtotal, shipping, COD surcharge,
 * discount, grand total) from the user's current cart so the frontend never
 * hardcodes prices.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record OrderCalculationRequest(

        @NotNull(message = "Payment method is required.")
        PaymentMethod paymentMethod,

        @NotNull(message = "Delivery type is required.")
        DeliveryType deliveryType,

        String promoCode
) {
}
