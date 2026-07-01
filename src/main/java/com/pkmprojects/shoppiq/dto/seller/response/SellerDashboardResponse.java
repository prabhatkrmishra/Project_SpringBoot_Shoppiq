package com.pkmprojects.shoppiq.dto.seller.response;

import java.math.BigDecimal;

/**
 * Seller-facing dashboard summary DTO.
 *
 * <p>Provides key metrics for the authenticated seller's store,
 * including product count, order count, revenue, and stock alerts.</p>
 *
 * @param totalProducts      total number of products owned by the seller
 * @param totalOrders        total number of orders containing seller's products
 * @param totalRevenue       total revenue from seller's items in paid orders
 * @param lowStockProducts   count of products with low stock (≤5)
 * @param outOfStockProducts count of products that are out of stock
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record SellerDashboardResponse(
        long totalProducts,
        long totalOrders,
        BigDecimal totalRevenue,
        long lowStockProducts,
        long outOfStockProducts
) {
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
