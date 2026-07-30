package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Customer-level aggregated analytics data for admin reports.
 *
 * <p>This record provides per-customer sales metrics including total
 * order count, lifetime spend, average order value, and the dates of
 * their first and most recent purchases. It is used in customer
 * analytics reports to help administrators identify high-value
 * customers, detect churn risk, and segment the user base for
 * targeted marketing campaigns.</p>
 *
 * <p>This is a read-only projection DTO designed for JPQL constructor
 * expressions. It carries no validation constraints and is never used
 * as a request body. Instances are produced by the reporting repository
 * and consumed by the admin report response assembly logic.</p>
 *
 * @param userId     unique identifier of the customer, corresponding to
 *                   the {@code User} entity's primary key
 * @param username   customer's display username for identification
 * @param email      customer's email address for contact and communication
 * @param orderCount total number of confirmed orders placed by this
 *                   customer across their lifetime on the platform
 * @param totalSpent lifetime monetary value of all qualifying orders
 *                   placed by this customer, in the platform's base currency
 * @param avgOrder   average monetary value per order, computed as
 *                   {@code totalSpent / orderCount}; useful for
 *                   customer segmentation
 * @param firstOrder date of the customer's very first qualifying order;
 *                   used for cohort analysis and tenure calculations
 * @param lastOrder  date of the customer's most recent qualifying order;
 *                   used for recency-based segmentation and churn detection
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CustomerAggData(
        /**
         * Unique identifier of the user.
         */
        Long userId,
        /**
         * Username.
         */
        String username,
        /**
         * Email address.
         */
        String email,
        /**
         * Total number of orders placed.
         */
        long orderCount,
        /**
         * Total amount spent across all orders.
         */
        BigDecimal totalSpent,
        /**
         * Average order value.
         */
        BigDecimal avgOrder,
        /**
         * Date of the first order.
         */
        LocalDate firstOrder,
        /**
         * Date of the most recent order.
         */
        LocalDate lastOrder
) {
}
