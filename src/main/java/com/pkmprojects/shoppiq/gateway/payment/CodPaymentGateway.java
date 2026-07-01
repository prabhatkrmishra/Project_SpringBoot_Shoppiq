package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import org.springframework.stereotype.Component;

/**
 * {@link PaymentGatewayStrategy} implementation for Cash on Delivery (COD) orders.
 *
 * <p>
 * COD payments involve no external gateway. The payment record is created in
 * {@code PENDING} status and remains there until delivery is confirmed.
 * The {@link #verify} method is a no-op since COD confirmation is handled
 * by the delivery workflow.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
public class CodPaymentGateway implements PaymentGatewayStrategy {

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
