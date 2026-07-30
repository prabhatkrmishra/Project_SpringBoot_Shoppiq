package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;

/**
 * Projection for aggregating revenue and order count by seller across paid orders.
 *
 * @author prabhatkrmishra
 * @since 1.5.0
 */
public interface SellerRevenueAggregate {

    /**
     * The seller identifier.
     *
     * @return seller ID
     */
    Long getSellerId();

    /**
     * The seller's business name.
     *
     * @return business name
     */
    String getBusinessName();

    /**
     * Total distinct order count for this seller.
     *
     * @return total orders
     */
    Long getTotalOrders();

    /**
     * Total revenue (sum of line-item subtotals) for this seller.
     *
     * @return total revenue
     */
    BigDecimal getTotalRevenue();
}
