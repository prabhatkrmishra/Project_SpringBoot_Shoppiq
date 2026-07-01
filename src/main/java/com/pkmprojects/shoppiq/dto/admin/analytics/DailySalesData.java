package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily sales data point for analytics.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record DailySalesData(
        LocalDate date,
        Long ordersCount,
        BigDecimal revenue
) {
}
