package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;

/**
 * Weekly sales data point for the admin analytics dashboard.
 *
 * <p>This record provides weekly aggregated sales metrics keyed by
 * ISO year and ISO week number. It powers the weekly trend chart on
 * the administrator dashboard and enables week-over-week performance
 * comparisons. Each instance represents one calendar week's worth of
 * order and revenue data.</p>
 *
 * <p>The record is designed for JPQL constructor expressions,
 * enabling efficient single-query hydration without N+1 overhead.
 * It is a read-only projection with no validation constraints and
 * is never used as a request body.</p>
 *
 * @param year        the ISO year (which may differ from the calendar year
 *                    at year boundaries), used as the primary grouping key
 * @param week        the ISO week number, ranging from 1 to 53
 * @param ordersCount total number of orders placed and confirmed
 *                    during this week
 * @param revenue     total monetary revenue generated from all qualifying
 *                    orders during this week, in the platform's base currency
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record WeeklySalesData(
        /**
         * ISO year.
         */
        Integer year,

        /**
         * ISO week number (1–53).
         */
        Integer week,

        /**
         * Number of orders placed in this week.
         */
        Long ordersCount,

        /**
         * Total revenue generated in this week.
         */
        BigDecimal revenue
) {
}
