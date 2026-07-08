package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the correct {@link PaymentGatewayStrategy} for a given
 * {@link PaymentMethod}.
 *
 * <p>All {@link PaymentGatewayStrategy} beans are collected at startup and
 * indexed by the gateway they {@link PaymentGatewayStrategy#supports() support}.
 * Mapping a payment method to its gateway keeps the service decoupled from
 * concrete implementations — adding a new gateway only requires a new strategy
 * bean plus a single entry in {@link #resolve(PaymentMethod)}.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
public class PaymentGatewayRegistry {

    private final Map<PaymentGateway, PaymentGatewayStrategy> byGateway;
    private final PaymentGatewayStrategy onlineFallback;

    public PaymentGatewayRegistry(List<PaymentGatewayStrategy> strategies) {
        Map<PaymentGateway, PaymentGatewayStrategy> map = new EnumMap<>(PaymentGateway.class);
        for (PaymentGatewayStrategy strategy : strategies) {
            map.put(strategy.supports(), strategy);
        }
        this.byGateway = map;
        this.onlineFallback = map.get(PaymentGateway.ONLINE);
    }

    /**
     * Resolves the gateway strategy for the given payment method.
     *
     * <ul>
     *   <li>{@code COD} → cash-on-delivery strategy.</li>
     *   <li>{@code UPI} → UPI strategy.</li>
     *   <li>{@code PAYPAL} → PayPal strategy.</li>
     *   <li>{@code STRIPE} → Stripe strategy.</li>
     *   <li>{@code CREDIT_CARD} / {@code ONLINE} → Razorpay (default online).</li>
     * </ul>
     *
     * <p>If the mapped gateway bean is absent, the generic online placeholder
     * is returned as a fallback.</p>
     *
     * @param method the payment method chosen by the customer
     * @return the appropriate {@link PaymentGatewayStrategy}
     */
    public PaymentGatewayStrategy resolve(PaymentMethod method) {
        PaymentGateway target = switch (method) {
            case COD -> PaymentGateway.NONE;
            case UPI -> PaymentGateway.UPI;
            case PAYPAL -> PaymentGateway.PAYPAL;
            case STRIPE -> PaymentGateway.STRIPE;
            case CREDIT_CARD, ONLINE -> PaymentGateway.RAZORPAY;
        };
        PaymentGatewayStrategy strategy = byGateway.get(target);
        if (strategy != null) {
            return strategy;
        }
        if (onlineFallback != null) {
            return onlineFallback;
        }
        throw new IllegalStateException("No payment gateway configured for method: " + method);
    }
}
