package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.admin.AdminPaymentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private final PaymentRepository paymentRepository;

    public AdminPaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminPaymentResponse> getAllPayments(PaymentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var paymentPage = (status != null)
                ? paymentRepository.findByPaymentStatus(status, pageable)
                : paymentRepository.findAll(pageable);

        return PageResponse.of(paymentPage, AdminPaymentResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with id '%d' was not found.".formatted(paymentId)));
        return AdminPaymentResponse.fromEntity(payment);
    }

    @Override
    public AdminPaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with id '%d' was not found.".formatted(paymentId)));

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw PaymentInvalidStateException.refundNotAllowed(paymentId, payment.getPaymentStatus());
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        return AdminPaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDashboardStats getPaymentDashboardStats() {
        long totalPayments = paymentRepository.count();
        long successfulPayments = paymentRepository.countByPaymentStatus(PaymentStatus.PAID);
        long failedPayments = paymentRepository.countByPaymentStatus(PaymentStatus.FAILED);
        long pendingPayments = paymentRepository.countByPaymentStatus(PaymentStatus.PENDING);

        BigDecimal totalRevenue = Optional.ofNullable(
                        paymentRepository.sumAmountByPaymentStatus(PaymentStatus.PAID))
                .orElse(BigDecimal.ZERO);

        BigDecimal refundedAmount = Optional.ofNullable(
                        paymentRepository.sumAmountByPaymentStatus(PaymentStatus.REFUNDED))
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