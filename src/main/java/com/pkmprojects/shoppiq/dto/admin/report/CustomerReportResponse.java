package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for customer report data.
 *
 * <p>
 * This DTO provides customer analytics for retention and
 * lifetime value analysis.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose customer metrics for reports.</li>
 *     <li>Support segmentation and retention analysis.</li>
 * </ul *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Includes RFM (Recency, Frequency, Monetary) metrics.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record CustomerReportResponse(

        /**
         * Report generation timestamp.
         */
        LocalDate generatedAt,

        /**
         * Total customers analyzed.
         */
        int totalCustomers,

        /**
         * Active customers (ordered in period).
         */
        int activeCustomers,

        /**
         * New customers in period.
         */
        int newCustomers,

        /**
         * Customer metrics rows.
         */
        List<CustomerReportRow> rows
) {

    /**
     * Individual customer metrics row.
     */
    public record CustomerReportRow(

            /**
             * Customer identifier.
             */
            Long userId,

            /**
             * Customer username.
             */
            String username,

            /**
             * Customer email.
             */
            String email,

            /**
             * Total orders placed.
             */
            long totalOrders,

            /**
             * Total amount spent.
             */
            BigDecimal totalSpent,

            /**
             * Average order value.
             */
            BigDecimal averageOrderValue,

            /**
             * Days since last order.
             */
            int daysSinceLastOrder,

            /**
             * Customer segment.
             */
            String segment
    ) {
    }
}