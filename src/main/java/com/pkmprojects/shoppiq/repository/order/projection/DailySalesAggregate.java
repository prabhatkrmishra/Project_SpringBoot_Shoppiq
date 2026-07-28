package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Projection for daily sales aggregation — returns the raw order timestamp
 * and grand total so the service can group by date in Java.
 *
 * <p>Eliminates loading full {@code Order} entities (with their entire
 * entity-graph) just to extract two scalar values.</p>
 */
public interface DailySalesAggregate {
    Instant getOrderDate();
    BigDecimal getRevenue();
}
