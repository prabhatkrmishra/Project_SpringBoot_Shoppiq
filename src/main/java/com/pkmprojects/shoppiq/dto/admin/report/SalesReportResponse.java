package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for sales report data.
 *
 * <p>
 * This DTO provides structured sales data for report generation
 * and export (PDF, Excel, CSV).
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose sales data for report generation.</li>
 *     <li>Support multiple export formats.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Nested records for each report row.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record SalesReportResponse(

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
         * Total orders in period.
         */
        long totalOrders,

        /**
         * Total revenue in period.
         */
        BigDecimal totalRevenue,

        /**
         * Average order value.
         */
        BigDecimal averageOrderValue,

        /**
         * Sales data rows.
         */
        List<SalesReportRow> rows
) {

    /**
     * Individual sales report row.
     */
    public record SalesReportRow(

            /**
             * Date of the order.
             */
            LocalDate date,

            /**
             * Order count for this date.
             */
            long orderCount,

            /**
             * Revenue for this date.
             */
            BigDecimal revenue,

            /**
             * Average order value for this date.
             */
            BigDecimal averageOrderValue
    ) {
    }
}