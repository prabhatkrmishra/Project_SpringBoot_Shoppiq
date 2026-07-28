package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily sales data point for analytics.
 *
 * <p>This <b>Java record</b> represents one row of daily sales data used in
 * the admin dashboard charts. It is designed for use with JPQL constructor
 * expressions ({@code SELECT NEW DailySalesData(...)}), which require a
 * matching constructor — automatically provided by the record.</p>
 *
 * <p><b>Role:</b> Nested inside {@link com.pkmprojects.shoppiq.dto.admin.response.SalesAnalyticsResponse
 * SalesAnalyticsResponse}. This is a <i>response projection</i>, not a request DTO.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record DailySalesData(
        LocalDate date,
        Long ordersCount,
        BigDecimal revenue
) {
}
