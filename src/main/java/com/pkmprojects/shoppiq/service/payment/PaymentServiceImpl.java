package com.pkmprojects.shoppiq.service.payment;

import com.pkmprojects.shoppiq.dto.payment.PaymentResponse;
import com.pkmprojects.shoppiq.dto.payment.PaymentStatusResponse;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.general.payment.DuplicatePaymentException;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentAccessDeniedException;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentInvalidStateException;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentNotFoundException;
import com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayRegistry;
import com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayStrategy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link PaymentService}.
 *
 * <p><strong>What this Service implementation demonstrates:</strong></p>
 * <ul>
 *   <li><strong>Strategy pattern via dependency injection</strong> — The
 *       {@link com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayRegistry} resolves the
 *       appropriate {@link com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayStrategy} based
 *       on the payment method (COD vs. ONLINE), demonstrating how to delegate external-system
 *       logic to pluggable strategies.</li>
 *   <li><strong>State machine enforcement</strong> — Each method checks the current
 *       {@link com.pkmprojects.shoppiq.enums.PaymentStatus} before proceeding, throwing
 *       {@link com.pkmprojects.shoppiq.exception.general.payment.PaymentInvalidStateException}
 *       for illegal transitions.</li>
 *   <li><strong>Duplicate payment prevention</strong> — {@link #createPayment} uses
 *       {@code paymentLookupService.existsByOrder()} to enforce one payment per order.</li>
 *   <li><strong>Idempotent operations</strong> — {@link #pay} returns immediately if already
 *       {@code PROCESSING}; {@link #verifyPayment} returns immediately if already
 *       {@code PAID}. This supports safe retries from the client.</li>
 *   <li><strong>Ownership assertion</strong> — The private {@code assertOwnership} method
 *       validates that the payment's order belongs to the authenticated user before allowing
 *       customer-facing operations.</li>
 *   <li><strong>Delegation to helper services</strong> — {@code PaymentLookupService} and
 *       {@code PaymentWriteService} are separated from business logic, showing single-responsibility
 *       decomposition at the service layer.</li>
 *   <li><strong>{@code @PreAuthorize} for admin operations</strong> — {@link #refund} is
 *       protected by Spring Security's method-level {@code hasRole('ADMIN')} check.</li>
 *   <li><strong>Payment reference generation</strong> — The formatted reference
 *       ({@code PAY-20240728-42}) is built in a private helper, demonstrating internal ID
 *       generation within the service layer.</li>
 * </ul>
 *
 * <h2>Business Rules Enforced</h2>
 * <ul>
 *   <li>One payment record per order — duplicate creation is rejected.</li>
 *   <li>Paid payments cannot be paid again.</li>
 *   <li>Failed payments can be retried via {@link #pay}.</li>
 *   <li>Only {@code PAID} payments can be refunded.</li>
 *   <li>Only {@code PENDING} or {@code FAILED} payments can be cancelled.</li>
 *   <li>Ownership is verified on every customer-facing operation.</li>
 * </ul>
 *
 * <h2>Payment Reference Format</h2>
 * <pre>PAY-yyyyMMdd-{orderId}</pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final String DEFAULT_CURRENCY = "INR";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PaymentLookupService paymentLookupService;
    private final PaymentWriteService paymentWriteService;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final Clock clock;

    public PaymentServiceImpl(PaymentLookupService paymentLookupService,
                              PaymentWriteService paymentWriteService,
                              PaymentGatewayRegistry gatewayRegistry,
                              Clock clock) {
        this.paymentLookupService = paymentLookupService;
        this.paymentWriteService = paymentWriteService;
        this.gatewayRegistry = gatewayRegistry;
        this.clock = clock;
    }

    // =========================================================
    // Create
    // =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Builds the payment record, generates an internal reference, and
     * delegates to the appropriate gateway strategy to set the initial state.
     * </p>
     */
    @Override
    public Payment createPayment(Order order) {

        if (paymentLookupService.existsByOrder(order)) {
            throw DuplicatePaymentException.forOrder(order.getId());
        }

        String reference = buildReference(order.getId());
        PaymentGatewayStrategy strategy = gatewayRegistry.resolve(order.getPaymentMethod());

        Payment payment = Payment.builder()
                .order(order)
                .paymentReference(reference)
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .gateway(PaymentGateway.NONE)
                .amount(order.getGrandTotal())
                .currency(DEFAULT_CURRENCY)
                .build();

        strategy.process(payment);

        return paymentWriteService.save(payment);
    }

    // =========================================================
    // Pay
    // =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Allowed from: {@code PENDING} or {@code FAILED}.
     * COD payments remain {@code PENDING}.
     * ONLINE payments move to {@code PROCESSING}.
     * </p>
     */
    @Override
    public PaymentStatusResponse pay(User user, Long paymentId) {
        Payment payment = findAndAssertOwnership(user, paymentId);

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw PaymentInvalidStateException.alreadyPaid(paymentId);
        }

        if (payment.getPaymentStatus() == PaymentStatus.CANCELLED
                || payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw PaymentInvalidStateException.cannotPay(paymentId, payment.getPaymentStatus());
        }

        if (payment.getPaymentStatus() == PaymentStatus.PROCESSING) {
            return PaymentStatusResponse.from(payment); // idempotent — already initiated
        }

        PaymentGatewayStrategy strategy = gatewayRegistry.resolve(payment.getPaymentMethod());
        strategy.process(payment);

        paymentWriteService.save(payment);
        return PaymentStatusResponse.from(payment);
    }

    // =========================================================
    // Verify
    // =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Resolves the payment by {@code paymentId} (ownership-checked), stamps the
     * supplied {@code transactionId}, and delegates to the gateway strategy to
     * mark the payment {@code PAID}.
     * </p>
     */
    @Override
    public PaymentStatusResponse verifyPayment(User user, Long paymentId, String transactionId) {
        Payment payment = findAndAssertOwnership(user, paymentId);

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            return PaymentStatusResponse.from(payment); // idempotent
        }

        if (payment.getPaymentStatus() != PaymentStatus.PROCESSING) {
            throw PaymentInvalidStateException.cannotVerify(paymentId, payment.getPaymentStatus());
        }

        PaymentGatewayStrategy strategy = gatewayRegistry.resolve(payment.getPaymentMethod());
        strategy.verify(payment, transactionId);

        paymentWriteService.save(payment);
        return PaymentStatusResponse.from(payment);
    }

    // =========================================================
    // Refund
    // =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>Admin-only operation. Refunds a {@code PAID} payment, transitioning it
     * to {@code REFUNDED} and recording the refund timestamp. Ownership is not
     * enforced because admins may refund any customer's payment.</p>
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentStatusResponse refund(User user, Long paymentId) {
        Payment payment = findOrThrow(paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw PaymentInvalidStateException.refundNotAllowed(paymentId, payment.getPaymentStatus());
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(clock.instant());

        paymentWriteService.save(payment);
        return PaymentStatusResponse.from(payment);
    }

    // =========================================================
    // Cancel
    // =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Only {@code PENDING} or {@code FAILED} payments may be cancelled.
     * </p>
     */
    @Override
    public PaymentStatusResponse cancelPayment(User user, Long paymentId) {
        Payment payment = findAndAssertOwnership(user, paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING
                && payment.getPaymentStatus() != PaymentStatus.FAILED) {
            throw PaymentInvalidStateException.cannotCancel(paymentId, payment.getPaymentStatus());
        }

        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        paymentWriteService.save(payment);
        return PaymentStatusResponse.from(payment);
    }

    // =========================================================
    // Get
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(User user, Long paymentId) {
        Payment payment = findAndAssertOwnership(user, paymentId);
        return PaymentResponse.from(payment);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Payment findOrThrow(Long paymentId) {
        return paymentLookupService.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.forId(paymentId));
    }

    private Payment findAndAssertOwnership(User user, Long paymentId) {
        Payment payment = findOrThrow(paymentId);
        assertOwnership(user, payment);
        return payment;
    }

    private void assertOwnership(User user, Payment payment) {
        if (payment.getOrder() == null
                || payment.getOrder().getUser() == null
                || !payment.getOrder().getUser().getId().equals(user.getId())) {
            throw PaymentAccessDeniedException.forPayment(payment.getId());
        }
    }

    /**
     * Generates an internal payment reference in the format {@code PAY-yyyyMMdd-{orderId}}.
     *
     * @param orderId the order ID
     * @return payment reference string
     */
    private String buildReference(Long orderId) {
        return "PAY-%s-%d".formatted(LocalDate.now(clock).format(DATE_FMT), orderId);
    }
}
