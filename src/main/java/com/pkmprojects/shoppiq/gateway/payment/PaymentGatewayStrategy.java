package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;

/**
 * Strategy interface for payment gateway integrations.
 *
 * <p>
 * Each implementation handles the payment lifecycle for a specific gateway
 * (COD, Razorpay, Stripe, PayPal). New gateways can be added without
 * modifying the checkout or payment service.
 * </p>
 *
 * <h2>Strategy Pattern</h2>
 * <pre>
 * PaymentGatewayStrategy (interface)
 *   ├── CodPaymentGateway       → gateway = NONE,     no external call
 *   ├── PlaceholderOnlineGateway → gateway = NONE,    simulated online
 *   ├── RazorpayGateway         → gateway = RAZORPAY  (future)
 *   ├── StripeGateway           → gateway = STRIPE    (future)
 *   └── PaypalGateway           → gateway = PAYPAL    (future)
 * </pre>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface PaymentGatewayStrategy {

    /**
     * Returns the {@link PaymentGateway} type handled by this strategy.
     *
     * @return gateway type
     */
    PaymentGateway supports();

    /**
     * Initiates payment processing for the given payment record.
     *
     * <p>
     * Implementations should update the payment's status, transactionId,
     * and gatewayResponse as appropriate.
     * </p>
     *
     * @param payment the payment to process
     */
    void process(Payment payment);

    /**
     * Verifies a payment using the external transaction ID returned by the gateway.
     *
     * <p>
     * Implementations should validate the transaction with the gateway and
     * update the payment status to {@code PAID} or {@code FAILED}.
     * </p>
     *
     * @param payment       the payment to verify
     * @param transactionId external transaction ID from the gateway
     */
    void verify(Payment payment, String transactionId);
}