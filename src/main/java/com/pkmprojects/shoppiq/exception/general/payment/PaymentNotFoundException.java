package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a requested
 * payment record cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) with factory methods for lookup by payment ID, transaction ID,
 * and order ID.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class PaymentNotFoundException extends ResourceNotFoundException {

    private PaymentNotFoundException(String detail) {
        super(ErrorCode.PAYMENT_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a payment not found by its identifier.
     *
     * @param id the payment ID
     * @return a new exception instance
     */
    public static PaymentNotFoundException forId(Long id) {
        return new PaymentNotFoundException(
                "Payment with id '%d' was not found.".formatted(id));
    }

    /**
     * Creates an exception for a payment not found by transaction ID.
     *
     * @param transactionId the transaction ID
     * @return a new exception instance
     */
    public static PaymentNotFoundException forTransactionId(String transactionId) {
        return new PaymentNotFoundException(
                "No payment found for transaction id '%s'.".formatted(transactionId));
    }

    /**
     * Creates an exception for a payment not found by order ID.
     *
     * @param orderId the order ID
     * @return a new exception instance
     */
    public static PaymentNotFoundException forOrder(Long orderId) {
        return new PaymentNotFoundException(
                "No payment found for order id '%d'.".formatted(orderId));
    }
}
