package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA interface-based projection for aggregating revenue and
 * order count by seller across paid orders.
 *
 * <p><strong>What this demonstrates:</strong></p>
 * <ul>
 *   <li><strong>Interface-based projection with aggregation</strong> — Spring Data JPA maps
 *       {@code SELECT} columns aliased with {@code AS} (e.g., {@code s.id AS sellerId}) to
 *       getter methods defined in this interface.</li>
 *   <li><strong>Multi-field projection</strong> — Combines scalar values ({@link #getSellerId()},
 *       {@link #getBusinessName()}) with aggregation results ({@link #getTotalOrders()},
 *       {@link #getTotalRevenue()}) in a single compile-time safe type.</li>
 *   <li><strong>Replaces Object[]</strong> — Eliminates fragile index-based access to
 *       {@code SELECT} results, improving readability and type safety.</li>
 * </ul>
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
