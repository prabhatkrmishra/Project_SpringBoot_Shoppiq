package com.pkmprojects.shoppiq.dto.payment;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for initiating or interacting with a payment.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record PaymentRequest(

        @NotNull(message = "Order ID is required.")
        Long orderId
) {
}
