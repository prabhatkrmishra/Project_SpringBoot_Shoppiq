package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily sales data point for the admin analytics dashboard.
 *
 * <p>This record represents a single day's aggregated sales figures,
 * including the total number of orders placed and the cumulative
 * revenue generated on that calendar date. It is consumed by the
 * sales analytics endpoint to render daily trend charts and sparkline
 * visualizations on the administrator dashboard.</p>
 *
 * <p>This DTO is designed for JPQL constructor expressions, meaning
 * the fields map directly to a {@code SELECT new ...} clause in the
 * repository query. It is a read-only projection with no validation
 * constraints and is never used as a request body. Instances are
 * created exclusively by the data access layer.</p>
 *
 * @param date        the calendar date this data point represents,
 *                    stored as a {@link java.time.LocalDate} without time zone
 * @param ordersCount total number of orders placed and confirmed on this date;
 *                    orders in PLACED or CANCELLED status may be excluded
 *                    depending on the query definition
 * @param revenue     total monetary revenue generated from all qualifying orders
 *                    on this date, expressed in the platform's base currency
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record DailySalesData(
        /**
         * Calendar date.
         */
        LocalDate date,

        /**
         * Number of orders placed on this date.
         */
        Long ordersCount,

        /**
         * Total revenue generated on this date.
         */
        BigDecimal revenue
) {
}
