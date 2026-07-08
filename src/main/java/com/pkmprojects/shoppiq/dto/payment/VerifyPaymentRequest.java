package com.pkmprojects.shoppiq.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for verifying an online payment.
 *
 * <p>
 * The payment is resolved by {@code paymentId} (the client already has it after
 * checkout). The {@code transactionId} is the simulated/real gateway transaction
 * reference that gets stamped onto the payment record and marks it {@code PAID}.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record VerifyPaymentRequest(

        @NotNull(message = "Payment ID is required.")
        Long paymentId,

        @NotBlank(message = "Transaction ID is required.")
        String transactionId
) {
}
