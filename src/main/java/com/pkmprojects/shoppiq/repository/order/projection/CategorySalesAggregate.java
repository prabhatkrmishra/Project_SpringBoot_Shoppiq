package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;

/**
 * Projection for top-categories aggregation.
 *
 * <p>Queries {@code OrderItem} with GROUP BY to compute aggregate sales
 * per category (quantity sold, revenue, unique products) directly in the
 * database.</p>
 */
public interface CategorySalesAggregate {

    Long getCategoryId();

    String getCategoryName();

    Long getQuantitySold();

    BigDecimal getRevenue();

    Long getUniqueProductsSold();
}
