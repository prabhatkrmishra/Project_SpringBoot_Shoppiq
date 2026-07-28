package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import org.springframework.stereotype.Component;

/**
 * <strong>Spring Boot Concept:</strong> Concrete implementation of
 * {@link PaymentGatewayStrategy} for Cash on Delivery (COD) orders —
 * a "no-op" strategy pattern implementation.
 *
 * <p>
 * COD payments involve no external gateway. The payment record is created in
 * {@code PENDING} status and remains there until delivery is confirmed.
 * The {@link #verify} method is a no-op since COD confirmation is handled
 * by the delivery workflow.
 * </p>
 *
 * <p><strong>Educational value:</strong> This class demonstrates the
 * <em>Null Object</em> / <em>No-Op Strategy</em> pattern in the context
 * of the Strategy pattern. Every payment method must have a strategy
 * implementation, even if it does nothing (like COD). By implementing
 * the full interface with no-op methods, the checkout service can treat
 * all payment methods uniformly — no special-case if/else for COD in the
 * service layer. The {@link PaymentGatewayRegistry} resolves the correct
 * strategy, and the service calls {@code process()} and {@code verify()}
 * polymorphically regardless of whether the gateway is real or simulated.
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public final class CodPaymentGateway implements PaymentGatewayStrategy {

    @Override
    public PaymentGateway supports() {
        return PaymentGateway.NONE;
    }

    /**
     * COD processing simply leaves the payment in {@code PENDING} status.
     * No external gateway call is made.
     *
     * @param payment the payment to process
     */
    @Override
    public void process(Payment payment) {
        // COD: no external gateway — stays PENDING until delivery confirmed
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setGatewayResponse("COD — awaiting delivery confirmation.");
    }

    /**
     * COD orders do not support online verification.
     * This method is a deliberate no-op; COD confirmation is a delivery concern.
     *
     * @param payment       the payment
     * @param transactionId unused for COD
     */
    @Override
    public void verify(Payment payment, String transactionId) {
        // COD does not support online verification; delivery workflow handles it
    }
}
