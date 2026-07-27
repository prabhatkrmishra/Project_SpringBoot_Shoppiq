package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentInvalidStateException;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentNotFoundException;
import com.pkmprojects.shoppiq.service.payment.PaymentLookupService;
import com.pkmprojects.shoppiq.service.payment.PaymentWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Default implementation of {@link AdminPaymentService}.
 *
 * <p>
 * Provides payment management operations for administrators
 * including retrieval, refunds, and dashboard statistics.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Retrieve paginated payments with optional status filter.</li>
 *     <li>Retrieve single payment by ID.</li>
 *     <li>Process refunds for PAID payments.</li>
 *     <li>Provide payment dashboard statistics.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>Read operations use read-only transactions.</li>
 *     <li>Refunds only allowed for PAID payments.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private final PaymentLookupService paymentLookupService;
    private final PaymentWriteService paymentWriteService;

    public AdminPaymentServiceImpl(PaymentLookupService paymentLookupService,
                                   PaymentWriteService paymentWriteService) {
        this.paymentLookupService = paymentLookupService;
        this.paymentWriteService = paymentWriteService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminPaymentResponse> getAllPayments(PaymentStatus status, int page, int size) {
        var paymentPage = (status != null)
                ? paymentLookupService.findByStatus(status, page, size)
                : paymentLookupService.findAll(page, size);

        return PageResponse.of(paymentPage, AdminPaymentResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentLookupService.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with id '%d' was not found.".formatted(paymentId)));
        return AdminPaymentResponse.fromEntity(payment);
    }

    @Override
    public AdminPaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentLookupService.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with id '%d' was not found.".formatted(paymentId)));

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw PaymentInvalidStateException.refundNotAllowed(paymentId, payment.getPaymentStatus());
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentWriteService.save(payment);

        return AdminPaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDashboardStats getPaymentDashboardStats() {
        long totalPayments = paymentLookupService.count();
        long successfulPayments = paymentLookupService.countByStatus(PaymentStatus.PAID);
        long failedPayments = paymentLookupService.countByStatus(PaymentStatus.FAILED);
        long pendingPayments = paymentLookupService.countByStatus(PaymentStatus.PENDING);

        BigDecimal totalRevenue = Optional.ofNullable(
                        paymentLookupService.sumAmountByStatus(PaymentStatus.PAID))
                .orElse(BigDecimal.ZERO);

        BigDecimal refundedAmount = Optional.ofNullable(
                        paymentLookupService.sumAmountByStatus(PaymentStatus.REFUNDED))
                .orElse(BigDecimal.ZERO);

        return new PaymentDashboardStats(
                totalPayments,
                successfulPayments,
                failedPayments,
                pendingPayments,
                totalRevenue,
                refundedAmount
        );
    }
}
