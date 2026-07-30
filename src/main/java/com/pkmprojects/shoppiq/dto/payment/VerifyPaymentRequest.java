package com.pkmprojects.shoppiq.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for verifying an online payment with the gateway.
 *
 * <p>This record is submitted to the payment verification endpoint after
 * the customer completes payment through the external gateway. The
 * payment is resolved by {@code paymentId} (the client already has it
 * from the checkout response), and the {@code transactionId} is the
 * gateway's transaction reference that gets stamped onto the payment
 * record and marks it as PAID.</p>
 *
 * <p>Both fields are required: a missing {@code paymentId} would leave
 * the payment record in an indeterminate state, while a missing
 * {@code transactionId} would prevent reconciliation with the payment
 * gateway. The service layer validates that the payment exists and is
 * in PENDING status before processing the verification.</p>
 *
 * @param paymentId     identifier of the payment record to verify;
 *                      must be a positive number referencing an existing
 *                      payment in PENDING status
 * @param transactionId gateway transaction reference code; must not
 *                      be blank; this is the unique identifier returned
 *                      by the payment gateway upon successful processing
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record VerifyPaymentRequest(

        /**
         * Payment ID. Must be positive.
         */
        @NotNull(message = "Payment ID is required.")
        @Positive(message = "Payment ID must be a positive number.")
        Long paymentId,

        /**
         * Gateway transaction reference. Must not be blank.
         */
        @NotBlank(message = "Transaction ID is required.")
        String transactionId
) {
}
