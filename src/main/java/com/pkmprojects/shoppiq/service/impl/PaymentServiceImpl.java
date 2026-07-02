package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.payment.PaymentResponse;
import com.pkmprojects.shoppiq.dto.payment.PaymentStatusResponse;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayRegistry;
import com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayStrategy;
import com.pkmprojects.shoppiq.repository.PaymentRepository;
import com.pkmprojects.shoppiq.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link PaymentService}.
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
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final String DEFAULT_CURRENCY = "INR";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRegistry gatewayRegistry;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PaymentGatewayRegistry gatewayRegistry) {
        this.paymentRepository = paymentRepository;
        this.gatewayRegistry = gatewayRegistry;
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

        if (paymentRepository.existsByOrder(order)) {
            throw new DuplicatePaymentException(order.getId());
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

        return paymentRepository.save(payment);
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

        PaymentGatewayStrategy strategy = gatewayRegistry.resolve(payment.getPaymentMethod());
        strategy.process(payment);

        paymentRepository.save(payment);
        return PaymentStatusResponse.from(payment);
    }

    // =========================================================
    // Verify
    // =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Looks up the payment by transactionId, verifies ownership,
     * and delegates to the gateway strategy to mark it {@code PAID}.
     * </p>
     */
    @Override
    public PaymentStatusResponse verifyPayment(User user, String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> PaymentNotFoundException.forTransactionId(transactionId));

        assertOwnership(user, payment);

        PaymentGatewayStrategy strategy = gatewayRegistry.resolve(payment.getPaymentMethod());
        strategy.verify(payment, transactionId);

        paymentRepository.save(payment);
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
        paymentRepository.save(payment);
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
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.forId(paymentId));
    }

    private Payment findAndAssertOwnership(User user, Long paymentId) {
        Payment payment = findOrThrow(paymentId);
        assertOwnership(user, payment);
        return payment;
    }

    private void assertOwnership(User user, Payment payment) {
        if (!payment.getOrder().getUser().getId().equals(user.getId())) {
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
        return "PAY-%s-%d".formatted(LocalDate.now().format(DATE_FMT), orderId);
    }
}