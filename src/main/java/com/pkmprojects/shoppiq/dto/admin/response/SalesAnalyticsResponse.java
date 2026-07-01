package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.dto.admin.analytics.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for admin sales analytics.
 *
 * <p>
 * This DTO provides comprehensive sales analytics data for the
 * administrator dashboard, including time-series data and top
 * performers across products and categories.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose sales analytics to the admin API.</li>
 *     <li>Aggregate complex time-series queries into a single response.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Uses top-level record types for JPQL constructor mapping.</li>
 *     <li>Revenue trends provided as a date-to-revenue map for charting.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record SalesAnalyticsResponse(
        List<DailySalesData> dailySales,
        List<WeeklySalesData> weeklySales,
        List<MonthlySalesData> monthlySales,
        List<TopSellingProductData> topSellingProducts,
        List<TopCategoryData> topCategories,
        Map<LocalDate, BigDecimal> revenueTrends
) {
}
