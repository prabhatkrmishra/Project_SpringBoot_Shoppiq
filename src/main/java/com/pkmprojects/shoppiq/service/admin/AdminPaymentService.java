package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminPaymentResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;

/**
 * Business contract for admin payment management and refund processing.
 *
 * <p>Defines operations for retrieving payments with filtering, processing
 * refunds for PAID payments, and computing payment dashboard statistics.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface AdminPaymentService {

    /**
     * Retrieves all payments with optional filtering.
     *
     * @param status optional status filter
     * @param page   page number (0-based)
     * @param size   page size
     * @return paginated payment responses
     */
    PageResponse<AdminPaymentResponse> getAllPayments(PaymentStatus status, int page, int size);

    /**
     * Retrieves a single payment by ID.
     *
     * @param paymentId payment identifier
     * @return payment response
     */
    AdminPaymentResponse getPaymentById(Long paymentId);

    /**
     * Processes a refund for a payment.
     *
     * <p>Only {@code PAID} payments can be refunded.</p>
     *
     * @param paymentId payment identifier
     * @return updated payment response
     */
    AdminPaymentResponse refundPayment(Long paymentId);

    /**
     * Retrieves payment dashboard statistics.
     *
     * @return payment statistics
     */
    PaymentDashboardStats getPaymentDashboardStats();

    /**
     * Payment dashboard statistics.
     */
    record PaymentDashboardStats(
            long totalPayments,
            long successfulPayments,
            long failedPayments,
            long pendingPayments,
            BigDecimal totalRevenue,
            BigDecimal refundedAmount
    ) {
    }
}
