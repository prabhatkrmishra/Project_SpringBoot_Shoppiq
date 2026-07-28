package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;

/**
 * <strong>Spring Boot Concept:</strong> Strategy interface defining the
 * contract for payment gateway integrations.
 *
 * <p>
 * Each implementation handles the payment lifecycle for a specific gateway
 * (COD, Razorpay, Stripe, PayPal, UPI). New gateways can be added without
 * modifying the checkout or payment service — the <em>open/closed principle</em>
 * in action (the service is closed for modification but open for extension).
 * </p>
 *
 * <p><strong>Educational value:</strong> This is the core of the
 * <strong>Strategy pattern</strong>. The interface defines two operations
 * ({@link #process} and {@link #verify}) and a discriminator ({@link #supports}).
 * Concrete strategies implement the gateway-specific logic, and the
 * {@link PaymentGatewayRegistry} selects the right strategy at runtime.
 * This decouples the payment service from any specific gateway provider —
 * adding Stripe, for example, means creating {@code StripeGateway} without
 * changing a single line in the checkout service.
 * </p>
 *
 * <h2>Strategy Hierarchy</h2>
 * <pre>
 * PaymentGatewayStrategy (interface)
 *   ├── AbstractRestGateway (abstract template for REST-based gateways)
 *   │   ├── RazorpayGateway
 *   │   ├── StripeGateway
 *   │   ├── PaypalGateway
 *   │   └── UpiGateway
 *   ├── CodPaymentGateway        (no external call, no-op verify)
 *   └── OnlinePaymentGateway     (profile-based dev placeholder)
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public sealed interface PaymentGatewayStrategy
        permits AbstractRestGateway, CodPaymentGateway, OnlinePaymentGateway {

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
