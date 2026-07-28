package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;

/**
 * Monthly sales data point for analytics.
 *
 * <p>This <b>Java record</b> provides monthly aggregated sales data
 * (year + month composite key). Like the other analytics records, it is
 * designed for JPQL constructor expressions, eliminating the need for
 * {@code @SqlResultSetMapping} or manual mapping.</p>
 *
 * <p><b>Role:</b> Response-only projection used in
 * {@link com.pkmprojects.shoppiq.dto.admin.response.SalesAnalyticsResponse}.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record MonthlySalesData(
        Integer year,
        Integer month,
        Long ordersCount,
        BigDecimal revenue
) {
}
