package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for the admin dashboard summary cards.
 *
 * <p>
 * This DTO aggregates key metrics displayed at the top of the
 * administrator dashboard. All counts are real-time snapshots
 * retrieved from the database.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose aggregated dashboard statistics to the admin API.</li>
 *     <li>Hide internal aggregation queries from the API consumer.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Created using {@link #from(User, Item, Order, Instant)}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * @param totalUsers            total registered users
     * @param totalProducts         total catalog products
     * @param totalOrders           total orders placed
     * @param todaysOrders          orders placed today
     * @param todaysRevenue         revenue generated today
     * @param pendingOrders         orders in PLACED status
     * @param cancelledOrders       cancelled orders
     * @param outOfStockProducts    products with zero stock
     * @param lowStockProducts      products below low-stock threshold
     * @return populated response DTO
     */
    public static DashboardSummaryResponse from(
            long totalUsers,
            long totalProducts,
            long totalOrders,
            long todaysOrders,
            BigDecimal todaysRevenue,
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
                todaysRevenue,
                pendingOrders,
                cancelledOrders,
                outOfStockProducts,
                lowStockProducts
        );
    }
}