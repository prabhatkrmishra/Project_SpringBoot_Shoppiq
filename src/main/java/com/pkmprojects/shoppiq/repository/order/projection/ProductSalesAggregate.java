package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;

/**
 * Projection for top-selling products aggregation.
 *
 * <p>Queries {@code OrderItem} with GROUP BY to compute aggregate sales
 * per product (quantity sold, revenue) directly in the database,
 * avoiding loading full entity graphs into memory.</p>
 */
public interface ProductSalesAggregate {

    Long getItemId();

    String getItemName();

    String getSku();

    Long getQuantitySold();

    BigDecimal getRevenue();
}
