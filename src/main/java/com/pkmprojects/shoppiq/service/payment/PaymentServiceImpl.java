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
 * {@link PaymentService} implementation handling payment creation, processing,
 * verification, cancellation, and refund with state machine enforcement and
 * ownership validation.
 *
 * @author prabhatkrmishra
 * @see PaymentService
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
