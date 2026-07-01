package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Placeholder {@link PaymentGatewayStrategy} for online payments.
 *
 * <p>
 * This implementation simulates online payment processing without calling
 * a real gateway. It is intended to be replaced with a concrete gateway
 * (Razorpay, Stripe, or PayPal) in a future phase by implementing
 * {@link PaymentGatewayStrategy} and registering it via the
 * {@link PaymentGatewayRegistry}.
 * </p>
 *
 * <h2>Simulated Behaviour</h2>
 * <ul>
 *   <li>{@link #process(Payment)} — moves payment to {@code PROCESSING},
 *       sets a dummy gateway response with a simulated payment URL.</li>
 *   <li>{@link #verify(Payment, String)} — accepts any non-blank transactionId
 *       and marks the payment as {@code PAID}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
public class OnlinePaymentGateway implements PaymentGatewayStrategy {

    private static final String PLACEHOLDER_PAYMENT_URL =
            "https://pay.shoppiq.dev/simulate?ref=%s";

    /**
     * This placeholder uses {@code NONE} so it acts as the fallback for
     * any ONLINE method not yet wired to a real gateway.
     *
     * <p>
     * When a real gateway (e.g. {@code RAZORPAY}) is added, it registers
     * with its own key and this implementation remains as the fallback.
     * </p>
     */
    @Override
    public PaymentGateway supports() {
        // Acts as the ONLINE placeholder until a real gateway replaces it
        return PaymentGateway.NONE;
    }

    /**
     * Simulates gateway processing: moves status to {@code PROCESSING}
     * and returns a dummy payment URL.
     *
     * @param payment the payment to process
     */
    @Override
    public void process(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.PROCESSING);

        String paymentUrl = PLACEHOLDER_PAYMENT_URL.formatted(payment.getPaymentReference());
        payment.setGatewayResponse(
                "{\"status\":\"INITIATED\",\"paymentUrl\":\"%s\"}".formatted(paymentUrl)
        );
    }

    /**
     * Simulates verification: accepts the provided transactionId and
     * marks the payment as {@code PAID}.
     *
     * @param payment       the payment to verify
     * @param transactionId simulated transaction ID from the client
     */
    @Override
    public void verify(Payment payment, String transactionId) {
        payment.setTransactionId(transactionId);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        payment.setGatewayResponse(
                "{\"status\":\"SUCCESS\",\"transactionId\":\"%s\"}".formatted(transactionId)
        );
    }
}
