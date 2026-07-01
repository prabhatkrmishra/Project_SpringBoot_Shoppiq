package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for inventory report data.
 *
 * <p>
 * This DTO provides comprehensive inventory health metrics
 * for stock management and procurement planning.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose inventory metrics for reports.</li>
 *     <li>Support reorder decisions.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Includes stock valuation and aging metrics.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record InventoryReportResponse(

        /**
         * Report generation timestamp.
         */
        LocalDate generatedAt,

        /**
         * Total products in catalog.
         */
        int totalProducts,

        /**
         * Total stock units.
         */
        int totalStockUnits,

        /**
         * Total stock value (at cost/price).
         */
        BigDecimal totalStockValue,

        /**
         * Products out of stock.
         */
        int outOfStockCount,

        /**
         * Products low on stock.
         */
        int lowStockCount,

        /**
         * Inventory detail rows.
         */
        List<InventoryReportRow> rows
) {

    /**
     * Individual inventory detail row.
     */
    public record InventoryReportRow(

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
             * Current stock.
             */
            int currentStock,

            /**
             * Reserved stock (in carts/orders).
             */
            int reservedStock,

            /**
             * Available stock.
             */
            int availableStock,

            /**
             * Stock value.
             */
            BigDecimal stockValue,

            /**
             * Reorder level.
             */
            int reorderLevel,

            /**
             * Days of supply remaining.
             */
            int daysOfSupply,

            /**
             * Stock status.
             */
            String status
    ) {
    }
}