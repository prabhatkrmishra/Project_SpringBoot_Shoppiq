package com.pkmprojects.shoppiq.dto.payment;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for verifying an online payment using the gateway transaction ID.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record VerifyPaymentRequest(

        @NotBlank(message = "Transaction ID is required.")
        String transactionId
) {
}
