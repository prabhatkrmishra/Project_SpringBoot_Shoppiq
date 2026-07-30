package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Projection for daily sales aggregation within a date range.
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface DailySalesAggregate {

    /**
     * The order timestamp.
     *
     * @return order date
     */
    Instant getOrderDate();

    /**
     * The total revenue for this day.
     *
     * @return revenue
     */
    BigDecimal getRevenue();
}
