package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;

/**
 * Weekly sales data point for analytics.
 *
 * <p>This <b>Java record</b> provides weekly aggregated sales data
 * (ISO year + week number composite key). It is populated via JPQL
 * constructor expressions in the analytics layer.</p>
 *
 * <p><b>Role:</b> Response-only projection inside
 * {@link com.pkmprojects.shoppiq.dto.admin.response.SalesAnalyticsResponse}.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record WeeklySalesData(
        Integer year,
        Integer week,
        Long ordersCount,
        BigDecimal revenue
) {
}
