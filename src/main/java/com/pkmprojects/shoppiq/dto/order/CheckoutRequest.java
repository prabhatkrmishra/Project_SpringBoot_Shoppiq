package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for placing an order at checkout.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record CheckoutRequest(

        @NotNull(message = "Address ID is required.")
        Long addressId,

        @NotNull(message = "Payment method is required.")
        PaymentMethod paymentMethod
) {
}
