package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentGatewayNotFoundException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry that collects all {@link PaymentGatewayStrategy} beans and resolves
 * the correct implementation for a given {@link PaymentMethod}.
 *
 * <p>This component acts as a factory that maps payment methods to their
 * corresponding gateway strategies. It is initialized at startup by
 * collecting all Spring-managed {@link PaymentGatewayStrategy} beans and
 * indexing them by their {@link PaymentGatewayStrategy#supports() supported}
 * gateway type. The {@link #resolve(PaymentMethod)} method performs the
 * lookup, translating the customer's payment method choice into the
 * appropriate gateway strategy.</p>
 *
 * <p>The registry includes a fallback mechanism: if the primary gateway
 * for a payment method is not available, it falls back to the generic
 * ONLINE placeholder strategy. If no fallback is available, it throws a
 * {@link PaymentGatewayNotFoundException}. This ensures that the checkout
 * flow can always proceed as long as at least one gateway is configured.</p>
 *
 * @author prabhatkrmishra
 * @see PaymentGatewayStrategy
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
        throw PaymentGatewayNotFoundException.forMethod(method.name());
    }
}
