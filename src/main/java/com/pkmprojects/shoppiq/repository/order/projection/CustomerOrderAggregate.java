package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Projection for aggregating per-customer order statistics within a date range.
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface CustomerOrderAggregate {

    /**
     * The user identifier.
     *
     * @return user ID
     */
    Long getUserId();

    /**
     * The username.
     *
     * @return username
     */
    String getUsername();

    /**
     * The email address.
     *
     * @return email
     */
    String getEmail();

    /**
     * Total order count for this customer.
     *
     * @return order count
     */
    Long getOrderCount();

    /**
     * Total amount spent by this customer.
     *
     * @return total spent
     */
    BigDecimal getTotalSpent();

    /**
     * Timestamp of the customer's first order.
     *
     * @return first order date
     */
    Instant getFirstOrderDate();

    /**
     * Timestamp of the customer's most recent order.
     *
     * @return last order date
     */
    Instant getLastOrderDate();
}
