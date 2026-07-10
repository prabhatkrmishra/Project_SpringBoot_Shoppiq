package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminPaymentResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;

/**
 * Business contract for admin payment management.
 *
 * <p>
 * Defines the operations for managing payments,
 * including retrieval, refunds, and dashboard statistics.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Retrieve all payments with pagination.</li>
 *     <li>Retrieve a single payment by ID.</li>
 *     <li>Process refund for a payment.</li>
 *     <li>Get payment dashboard statistics.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Works exclusively with DTOs.</li>
 *     <li>Refunds only allowed for PAID payments.</li>
 *     <li>Implemented by {@code AdminPaymentServiceImpl}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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