package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.payment.PaymentResponse;
import com.pkmprojects.shoppiq.dto.payment.PaymentStatusResponse;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.entity.User;

/**
 * Contract for payment lifecycle operations in Shoppiq.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface PaymentService {

    /**
     * Creates and persists a payment record for the given order.
     *
     * <p>Called automatically at the end of a successful checkout.</p>
     *
     * @param order the newly created order
     * @return the persisted payment entity
     */
    Payment createPayment(Order order);

    /**
     * Initiates payment processing for the given payment id.
     *
     * <p>
     * COD payments remain {@code PENDING}; online payments move to
     * {@code PROCESSING} and a simulated URL is stored in the gateway response.
     * </p>
     *
     * @param user      authenticated customer
     * @param paymentId the payment to process
     * @return updated payment status response
     */
    PaymentStatusResponse pay(User user, Long paymentId);

    /**
     * Verifies an online payment using the external transaction ID.
     *
     * <p>
     * The payment is resolved by {@code paymentId} (ownership-checked) because
     * the {@code transactionId} is only stamped during verification. The supplied
     * transaction ID is then recorded and the payment is marked {@code PAID}.
     * </p>
     *
     * @param user          authenticated customer
     * @param paymentId     id of the payment to verify
     * @param transactionId gateway-issued (or simulated) transaction ID
     * @return updated payment status response
     */
    PaymentStatusResponse verifyPayment(User user, Long paymentId, String transactionId);

    /**
     * Cancels a payment that has not yet been completed.
     *
     * @param user      authenticated customer
     * @param paymentId the payment to cancel
     * @return updated payment status response
     */
    PaymentStatusResponse cancelPayment(User user, Long paymentId);

    /**
     * Refunds a completed payment.
     *
     * <p>Only {@code PAID} payments may be refunded; any other state is rejected.
     * This is an admin-only operation — ownership is intentionally <em>not</em>
     * enforced because an admin may refund any customer's payment.</p>
     *
     * @param user      authenticated admin
     * @param paymentId the payment to refund
     * @return updated payment status response
     */
    PaymentStatusResponse refund(User user, Long paymentId);

    /**
     * Returns the full payment detail for the given payment id.
     *
     * @param user      authenticated customer (ownership is validated)
     * @param paymentId the payment to retrieve
     * @return full payment response
     */
    PaymentResponse getPayment(User user, Long paymentId);
}
