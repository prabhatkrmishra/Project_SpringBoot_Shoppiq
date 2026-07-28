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
 * <p><b>Validation:</b> {@code @NotNull} on {@code paymentId} and
 * {@code @NotBlank} on {@code transactionId} ensure both fields are always
 * provided — critical because missing values could lead to payment records
 * stuck in PENDING state without a corresponding gateway transaction.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record VerifyPaymentRequest(

        @NotNull(message = "Payment ID is required.")
        Long paymentId,

        @NotBlank(message = "Transaction ID is required.")
        String transactionId
) {
}
