package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;

/**
 * Projection for product performance aggregation — computes per-product
 * sales statistics (quantity, revenue, average price) directly in the
 * database using GROUP BY.
 *
 * <p>Current stock is joined from {@code ItemDetails} separately since
 * it is not an aggregate function result.</p>
 */
public interface ProductPerformanceAggregate {

    Long getItemId();

    String getItemName();

    String getSku();

    Long getQuantitySold();

    BigDecimal getRevenue();

    BigDecimal getAveragePrice();

    Integer getCurrentStock();
}
