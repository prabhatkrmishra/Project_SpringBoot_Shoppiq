package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;

/**
 * Monthly sales data point for the admin analytics dashboard.
 *
 * <p>This record provides monthly aggregated sales metrics keyed by
 * calendar year and month. It powers the monthly trend chart on the
 * administrator dashboard and is also used for year-over-year
 * comparison views. Each instance represents one month's worth of
 * order and revenue data.</p>
 *
 * <p>The record is designed for JPQL constructor expressions,
 * enabling efficient single-query hydration without N+1 overhead.
 * It is a read-only projection with no validation constraints and
 * is never used as a request body.</p>
 *
 * @param year        the calendar year (e.g. 2025), used as the primary
 *                    grouping key in time-series queries
 * @param month       the calendar month as an integer from 1 (January)
 *                    to 12 (December)
 * @param ordersCount total number of orders placed and confirmed
 *                    during this month
 * @param revenue     total monetary revenue generated from all qualifying
 *                    orders during this month, in the platform's base currency
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record MonthlySalesData(
        /**
         * Calendar year.
         */
        Integer year,

        /**
         * Calendar month (1–12).
         */
        Integer month,

        /**
         * Number of orders placed in this month.
         */
        Long ordersCount,

        /**
         * Total revenue generated in this month.
         */
        BigDecimal revenue
) {
}
