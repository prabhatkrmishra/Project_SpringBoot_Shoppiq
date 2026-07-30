package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * No-op payment gateway strategy for Cash on Delivery (COD) orders.
 *
 * <p>COD payments involve no external gateway integration. The payment
 * record is created in PENDING status and remains there until the order
 * delivery is confirmed by the customer or delivery agent. This strategy
 * implements the {@link PaymentGatewayStrategy} interface as a no-op,
 * since COD does not require any API calls to external services.</p>
 *
 * <p>The COD gateway supports the {@link PaymentGateway#NONE} type and
 * is registered as a Spring bean. It is resolved by the
 * {@link PaymentGatewayRegistry} when a customer selects the COD payment
 * method at checkout.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
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
        log.debug("COD payment {} processed (stays PENDING until delivery)", payment.getId());
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
        log.debug("COD verify called for payment {} — no-op (delivery workflow handles confirmation)", payment.getId());
    }
}
