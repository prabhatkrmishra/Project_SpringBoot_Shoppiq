package com.pkmprojects.shoppiq.dto.seller.response;

import java.math.BigDecimal;

/**
 * Seller-facing dashboard summary DTO for store performance overview.
 *
 * <p>This record provides key metrics for the authenticated seller's
 * store, including product count, order volume, revenue, and inventory
 * health indicators. It is returned by the seller dashboard endpoint
 * and is displayed at the top of the seller's management UI to provide
 * an at-a-glance view of store performance.</p>
 *
 * <p>The static {@link #from} factory method accepts pre-computed
 * aggregate counts from the repository layer. The {@code lowStockProducts}
 * and {@code outOfStockProducts} counts help sellers identify
 * inventory issues that need immediate attention. Revenue figures
 * include only confirmed/paid orders.</p>
 *
 * @param totalProducts      total number of products listed by this seller
 *                           across all publishing states
 * @param totalOrders        total number of orders containing this seller's
 *                           products across all order statuses
 * @param totalRevenue       aggregate monetary revenue from this seller's
 *                           products in confirmed/paid orders, in the
 *                           platform's base currency
 * @param lowStockProducts   count of products with inventory at or below
 *                           the low-stock threshold (5 units); indicates
 *                           items at risk of stockout
 * @param outOfStockProducts count of products with zero inventory;
 *                           indicates items requiring immediate restocking
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record SellerDashboardResponse(
        /**
         * Total number of products owned by the seller.
         */
        long totalProducts,

        /**
         * Total number of orders containing seller's products.
         */
        long totalOrders,

        /**
         * Total revenue from seller's items in paid orders.
         */
        BigDecimal totalRevenue,

        /**
         * Count of products with low stock (≤5).
         */
        long lowStockProducts,

        /**
         * Count of products that are out of stock.
         */
        long outOfStockProducts
) {
    /**
     * Creates a dashboard summary response from aggregate counts.
     *
     * @param totalProducts      total number of products
     * @param totalOrders        total number of orders
     * @param totalRevenue       total revenue from paid orders
     * @param lowStockProducts   count of low-stock products
     * @param outOfStockProducts count of out-of-stock products
     * @return populated response DTO
     */
    public static SellerDashboardResponse from(
            long totalProducts,
            long totalOrders,
            BigDecimal totalRevenue,
            long lowStockProducts,
            long outOfStockProducts
    ) {
        return new SellerDashboardResponse(
                totalProducts, totalOrders, totalRevenue,
                lowStockProducts, outOfStockProducts
        );
    }
}
