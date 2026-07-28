package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Projection for customer aggregation — computes per-customer order
 * statistics directly in the database.
 *
 * <p>Eliminates loading full {@code Order} entities and their user
 * associations just to aggregate customer-level metrics.</p>
 */
public interface CustomerOrderAggregate {

    Long getUserId();

    String getUsername();

    String getEmail();

    Long getOrderCount();

    BigDecimal getTotalSpent();

    Instant getFirstOrderDate();

    Instant getLastOrderDate();
}
