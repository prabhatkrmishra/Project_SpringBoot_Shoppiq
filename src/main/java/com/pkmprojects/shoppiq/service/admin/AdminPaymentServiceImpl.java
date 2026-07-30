package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminPaymentResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentInvalidStateException;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentNotFoundException;
import com.pkmprojects.shoppiq.service.payment.PaymentLookupService;
import com.pkmprojects.shoppiq.service.payment.PaymentWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * {@link AdminPaymentService} implementation that handles payment retrieval,
 * refund processing, and dashboard statistics computation.
 *
 * @author prabhatkrmishra
 * @see AdminPaymentService
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private final PaymentLookupService paymentLookupService;
    private final PaymentWriteService paymentWriteService;
    private final Clock clock;

    public AdminPaymentServiceImpl(PaymentLookupService paymentLookupService,
                                   PaymentWriteService paymentWriteService,
                                   Clock clock) {
        this.paymentLookupService = paymentLookupService;
        this.paymentWriteService = paymentWriteService;
        this.clock = clock;
    }

    /**
     * Retrieves a paginated list of payments with optional status filtering.
     *
     * @param status optional payment status filter
     * @param page   zero-based page index
     * @param size   page size
     * @return paginated payment responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminPaymentResponse> getAllPayments(PaymentStatus status, int page, int size) {
        var paymentPage = (status != null)
                ? paymentLookupService.findByStatus(status, page, size)
                : paymentLookupService.findAll(page, size);

        return PageResponse.of(paymentPage, AdminPaymentResponse::fromEntity);
    }

    /**
     * Retrieves a single payment by ID.
     *
     * @param paymentId payment ID
     * @return payment response
     * @throws PaymentNotFoundException if the payment does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public AdminPaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentLookupService.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.forId(paymentId));
        return AdminPaymentResponse.fromEntity(payment);
    }

    /**
     * Processes a refund for a PAID payment — transitions status to REFUNDED.
     *
     * @param paymentId payment ID
     * @return updated payment response
     * @throws PaymentNotFoundException     if the payment does not exist
     * @throws PaymentInvalidStateException if the payment is not PAID
     */
    @Override
    public AdminPaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentLookupService.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.forId(paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw PaymentInvalidStateException.refundNotAllowed(paymentId, payment.getPaymentStatus());
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(Instant.now(clock));
        paymentWriteService.save(payment);

        return AdminPaymentResponse.fromEntity(payment);
    }

    /**
     * Computes payment dashboard statistics including counts by status, total revenue, and refunded amount.
     *
     * @return payment dashboard stats
     */
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
