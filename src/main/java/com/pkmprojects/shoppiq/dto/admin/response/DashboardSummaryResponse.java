package com.pkmprojects.shoppiq.dto.admin.response;

import java.math.BigDecimal;

/**
 * Response DTO for the admin dashboard summary cards.
 *
 * <p>This record aggregates key platform metrics displayed at the top
 * of the administrator dashboard. It provides a snapshot of user
 * registration, product catalog, order volume, and revenue figures
 * including today's performance and inventory health indicators. The
 * data is refreshed on each dashboard load to ensure administrators
 * see current figures.</p>
 *
 * <p>The static {@link #from} factory method accepts pre-computed
 * aggregate counts and handles null-safe conversion of monetary
 * values, defaulting to {@code BigDecimal.ZERO} when the underlying
 * query returns null. This ensures the frontend always receives
 * valid numeric values without null-checking.</p>
 *
 * @param totalUsers         total number of registered user accounts on the platform
 * @param totalProducts      total number of products in the catalog, including
 *                           products in all publishing states
 * @param totalOrders        total number of orders placed across all statuses
 * @param todaysOrders       number of orders placed on the current calendar day
 * @param todaysRevenue      total monetary revenue generated today from
 *                           confirmed orders, in the platform's base currency
 * @param totalRevenue       cumulative revenue from all confirmed orders across
 *                           the platform's entire history
 * @param pendingOrders      number of orders currently in PLACED status
 *                           awaiting seller or admin confirmation
 * @param cancelledOrders    total number of orders that have been cancelled
 * @param outOfStockProducts number of products with zero inventory;
 *                           indicates items requiring restocking
 * @param lowStockProducts   number of products with inventory below the
 *                           configured low-stock threshold; indicates
 *                           items at risk of stockout
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record DashboardSummaryResponse(

        /**
         * Total number of registered users.
         */
        long totalUsers,

        /**
         * Total number of catalog products.
         */
        long totalProducts,

        /**
         * Total number of orders placed.
         */
        long totalOrders,

        /**
         * Number of orders placed today.
         */
        long todaysOrders,

        /**
         * Total revenue generated today.
         */
        BigDecimal todaysRevenue,

        /**
         * Total revenue from all paid orders.
         */
        BigDecimal totalRevenue,

        /**
         * Number of orders in {@code PLACED} status awaiting confirmation.
         */
        long pendingOrders,

        /**
         * Number of cancelled orders.
         */
        long cancelledOrders,

        /**
         * Number of products with zero stock.
         */
        long outOfStockProducts,

        /**
         * Number of products below the low-stock threshold.
         */
        long lowStockProducts

) {

    /**
     * Creates a {@code DashboardSummaryResponse} from aggregate counts.
     *
     * @param totalUsers         total registered users
     * @param totalProducts      total catalog products
     * @param totalOrders        total orders placed
     * @param todaysOrders       orders placed today
     * @param todaysRevenue      revenue generated today
     * @param pendingOrders      orders in PLACED status
     * @param cancelledOrders    cancelled orders
     * @param outOfStockProducts products with zero stock
     * @param lowStockProducts   products below low-stock threshold
     * @return populated response DTO
     */
    public static DashboardSummaryResponse from(
            long totalUsers,
            long totalProducts,
            long totalOrders,
            long todaysOrders,
            BigDecimal todaysRevenue,
            BigDecimal totalRevenue,
            long pendingOrders,
            long cancelledOrders,
            long outOfStockProducts,
            long lowStockProducts
    ) {
        return new DashboardSummaryResponse(
                totalUsers,
                totalProducts,
                totalOrders,
                todaysOrders,
                todaysRevenue != null ? todaysRevenue : BigDecimal.ZERO,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                pendingOrders,
                cancelledOrders,
                outOfStockProducts,
                lowStockProducts
        );
    }
}
