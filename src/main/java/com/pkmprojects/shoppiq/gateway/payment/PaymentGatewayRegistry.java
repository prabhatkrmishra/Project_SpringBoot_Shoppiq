package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.enums.PaymentMethod;
import org.springframework.stereotype.Component;

/**
 * Resolves the correct {@link PaymentGatewayStrategy} for a given
 * {@link PaymentMethod}.
 *
 * <p>
 * This registry decouples the payment service from concrete gateway
 * implementations. Adding a new gateway only requires:
 * <ol>
 *   <li>Implementing {@link PaymentGatewayStrategy}.</li>
 *   <li>Registering it here via an additional injection or map entry.</li>
 * </ol>
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
public class PaymentGatewayRegistry {

    private final CodPaymentGateway codGateway;
    private final OnlinePaymentGateway onlineGateway;

    public PaymentGatewayRegistry(CodPaymentGateway codGateway,
                                  OnlinePaymentGateway onlineGateway) {
        this.codGateway = codGateway;
        this.onlineGateway = onlineGateway;
    }

    /**
     * Resolves the gateway strategy for the given payment method.
     *
     * @param method the payment method chosen by the customer
     * @return the appropriate {@link PaymentGatewayStrategy}
     */
    public PaymentGatewayStrategy resolve(PaymentMethod method) {
        return switch (method) {
            case COD -> codGateway;
            case ONLINE -> onlineGateway;
        };
    }
}