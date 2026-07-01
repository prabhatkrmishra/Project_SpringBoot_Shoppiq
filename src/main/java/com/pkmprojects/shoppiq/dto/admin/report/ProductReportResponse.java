package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for product report data.
 *
 * <p>
 * This DTO provides product performance metrics for inventory
 * and merchandising analysis.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose product sales performance for reports.</li>
 *     <li>Support inventory planning decisions.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Includes sales velocity and stock health indicators.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ProductReportResponse(

        /**
         * Report generation timestamp.
         */
        LocalDate generatedAt,

        /**
         * Total products analyzed.
         */
        int totalProducts,

        /**
         * Product performance rows.
         */
        List<ProductReportRow> rows
) {

    /**
     * Individual product performance row.
     */
    public record ProductReportRow(

            /**
             * Product identifier.
             */
            Long itemId,

            /**
             * Product name.
             */
            String itemName,

            /**
             * Product SKU.
             */
            String sku,

            /**
             * Category name.
             */
            String category,

            /**
             * Current stock quantity.
             */
            int stockQuantity,

            /**
             * Units sold in period.
             */
            long unitsSold,

            /**
             * Revenue generated.
             */
            BigDecimal revenue,

            /**
             * Average selling price.
             */
            BigDecimal averageSellingPrice,

            /**
             * Days of inventory remaining (based on sales velocity).
             */
            int daysOfInventory,

            /**
             * Stock health status.
             */
            String stockHealth
    ) {
    }
}