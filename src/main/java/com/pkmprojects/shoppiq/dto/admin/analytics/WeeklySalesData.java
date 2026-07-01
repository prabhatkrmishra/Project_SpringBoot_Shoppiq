package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;

/**
 * Weekly sales data point for analytics.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record WeeklySalesData(
        Integer year,
        Integer week,
        Long ordersCount,
        BigDecimal revenue
) {
}
