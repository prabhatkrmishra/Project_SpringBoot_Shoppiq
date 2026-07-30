package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;

/**
 * Strategy interface defining the contract for payment gateway integrations.
 *
 * <p>Each implementation handles the complete payment lifecycle for a
 * specific gateway provider. The interface defines three methods:
 * {@link #supports()} identifies the gateway type, {@link #process(Payment)}
 * initiates payment processing, and {@link #verify(Payment, String)}
 * confirms a payment using the external transaction ID.</p>
 *
 * <p>New gateways can be added by implementing this interface and
 * registering a Spring {@code @Component} bean. The
 * {@link PaymentGatewayRegistry} automatically discovers the bean and
 * makes it available for payment processing. This design follows the
 * Open/Closed Principle: the checkout and payment services are open
 * for extension (new gateways) but closed for modification.</p>
 *
 * @author prabhatkrmishra
 * @see PaymentGatewayRegistry
 * @since 1.0.0
 */
public sealed interface PaymentGatewayStrategy
        permits AbstractRestGateway, CodPaymentGateway, OnlinePaymentGateway {

    /**
     * Returns the {@link PaymentGateway} type handled by this strategy.
     *
     * <p>The returned value is used by the {@link PaymentGatewayRegistry}
     * to index this strategy for lookup. Each strategy must return a
     * unique gateway type. For example, the Razorpay implementation
     * returns {@link PaymentGateway#RAZORPAY}.</p>
     *
     * @return the gateway type this strategy handles
     */
    PaymentGateway supports();

    /**
     * Initiates payment processing for the given payment record.
     *
     * <p>Implementations should update the payment's status, transactionId,
     * and gatewayResponse as appropriate. For online gateways, this typically
     * involves calling the provider's API to create a payment order. For
     * COD, this is a no-op that leaves the payment in PENDING status.</p>
     *
     * @param payment the payment to process
     */
    void process(Payment payment);

    /**
     * Verifies a payment using the external transaction ID returned by the gateway.
     *
     * <p>Implementations should validate the transaction with the gateway
     * and update the payment status to {@code PAID} or {@code FAILED}.
     * For online gateways, this involves calling the provider's verification
     * API. For COD, this is a no-op as verification is handled by the
     * delivery workflow.</p>
     *
     * @param payment       the payment to verify
     * @param transactionId external transaction ID from the gateway
     */
    void verify(Payment payment, String transactionId);
}
