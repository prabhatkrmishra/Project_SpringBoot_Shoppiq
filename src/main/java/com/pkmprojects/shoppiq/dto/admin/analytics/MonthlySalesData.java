package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;

/**
 * Monthly sales data point for analytics.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record MonthlySalesData(
        Integer year,
        Integer month,
        Long ordersCount,
        BigDecimal revenue
) {
}
