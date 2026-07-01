package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for revenue report data.
 *
 * <p>
 * This DTO provides revenue breakdown by payment method, status,
 * and time period for financial reporting.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose revenue analytics for report generation.</li>
 *     <li>Support financial reconciliation.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Includes payment method and status breakdowns.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record RevenueReportResponse(

        /**
         * Report generation timestamp.
         */
        LocalDate generatedAt,

        /**
         * Report period start date.
         */
        LocalDate periodStart,

        /**
         * Report period end date.
         */
        LocalDate periodEnd,

        /**
         * Total revenue.
         */
        BigDecimal totalRevenue,

        /**
         * Revenue by payment method.
         */
        List<PaymentMethodRevenue> byPaymentMethod,

        /**
         * Revenue by payment status.
         */
        List<PaymentStatusRevenue> byPaymentStatus,

        /**
         * Daily revenue trend.
         */
        List<DailyRevenue> dailyTrend
) {

    /**
     * Revenue by payment method.
     */
    public record PaymentMethodRevenue(

            /**
             * Payment method.
             */
            String paymentMethod,

            /**
             * Total revenue.
             */
            BigDecimal revenue,

            /**
             * Transaction count.
             */
            long count
    ) {
    }

    /**
     * Revenue by payment status.
     */
    public record PaymentStatusRevenue(

            /**
             * Payment status.
             */
            String paymentStatus,

            /**
             * Total revenue.
             */
            BigDecimal revenue,

            /**
             * Transaction count.
             */
            long count
    ) {
    }

    /**
     * Daily revenue trend point.
     */
    public record DailyRevenue(

            /**
             * Date.
             */
            LocalDate date,

            /**
             * Revenue for this date.
             */
            BigDecimal revenue
    ) {
    }
}