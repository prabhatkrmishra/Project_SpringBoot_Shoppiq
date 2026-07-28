package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * <strong>Spring Boot Concept:</strong> Registry that collects all
 * {@link PaymentGatewayStrategy} beans at startup via constructor injection
 * and resolves the correct implementation for a given {@link PaymentMethod}.
 *
 * <p>All {@link PaymentGatewayStrategy} beans are collected at startup and
 * indexed by the gateway they {@link PaymentGatewayStrategy#supports() support}.
 * Mapping a payment method to its gateway keeps the service decoupled from
 * concrete implementations — adding a new gateway only requires a new strategy
 * bean plus a single entry in {@link #resolve(PaymentMethod)}.</p>
 *
 * <p><strong>Educational value:</strong> This class demonstrates Spring's
 * <strong>Bean aggregation / Registry</strong> pattern:
 * <ul>
 *   <li>Spring auto-injects all beans implementing {@code PaymentGatewayStrategy}
 *       into the constructor as a {@code List}.</li>
 *   <li>The registry indexes them by their {@code supports()} return value
 *       into an {@link java.util.EnumMap} for O(1) lookup.</li>
 *   <li>The checkout service only depends on {@code PaymentGatewayRegistry}
 *       (not on individual gateway beans), so adding a new payment provider
 *       is purely additive — no existing code changes.</li>
 *   <li>This is the <strong>Strategy + Registry</strong> pattern: the Strategy
 *       interface defines the contract, and the Registry acts as a
 *       <em>context</em> that selects the right strategy at runtime.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public class PaymentGatewayRegistry {

    private final Map<PaymentGateway, PaymentGatewayStrategy> byGateway;
    private final PaymentGatewayStrategy onlineFallback;

    /**
     * Collects all {@link PaymentGatewayStrategy} beans and indexes them
     * by their {@link PaymentGatewayStrategy#supports() supported} gateway.
     *
     * @param strategies all strategy beans discovered by Spring
     */
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
