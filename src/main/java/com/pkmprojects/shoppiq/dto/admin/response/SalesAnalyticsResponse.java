package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.dto.admin.analytics.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for the admin sales analytics dashboard.
 *
 * <p>This record provides comprehensive sales analytics data for the
 * administrator dashboard including time-series data at daily, weekly,
 * and monthly granularities, top-performing products and categories,
 * and period-specific revenue and order count summaries. It is returned
 * by the sales analytics endpoint and powers the charting and
 * reporting widgets on the admin dashboard.</p>
 *
 * <p>The time-series lists ({@code dailySales}, {@code weeklySales},
 * {@code monthlySales}) each contain data points designed for JPQL
 * constructor expressions. The {@code revenueTrends} map provides
 * a date-keyed view for simple line chart rendering. The period
 * totals ({@code todayRevenue}, {@code weekRevenue}, etc.) are
 * pre-computed for display in summary cards without additional
 * client-side aggregation.</p>
 *
 * @param dailySales         list of daily sales data points for the current
 *                           reporting period, ordered chronologically
 * @param weeklySales        list of weekly sales data points for the current
 *                           reporting period, ordered chronologically
 * @param monthlySales       list of monthly sales data points for the current
 *                           reporting period, ordered chronologically
 * @param topSellingProducts list of top-performing products ranked by
 *                           revenue contribution
 * @param topCategories      list of top-performing categories ranked by
 *                           revenue contribution
 * @param revenueTrends      map of calendar dates to revenue amounts for
 *                           charting daily revenue trends
 * @param todayRevenue       total monetary revenue generated on the current day
 * @param weekRevenue        total monetary revenue generated in the current week
 * @param monthRevenue       total monetary revenue generated in the current month
 * @param todayOrders        number of orders placed on the current day
 * @param weekOrders         number of orders placed in the current week
 * @param monthOrders        number of orders placed in the current month
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record SalesAnalyticsResponse(
        /**
         * Daily sales data for the current period.
         */
        List<DailySalesData> dailySales,

        /**
         * Weekly sales data for the current period.
         */
        List<WeeklySalesData> weeklySales,

        /**
         * Monthly sales data for the current period.
         */
        List<MonthlySalesData> monthlySales,

        /**
         * Top-selling products by revenue.
         */
        List<TopSellingProductData> topSellingProducts,

        /**
         * Top-performing categories by revenue.
         */
        List<TopCategoryData> topCategories,

        /**
         * Date-to-revenue map for charting.
         */
        Map<LocalDate, BigDecimal> revenueTrends,

        /**
         * Total revenue generated today.
         */
        BigDecimal todayRevenue,

        /**
         * Total revenue generated this week.
         */
        BigDecimal weekRevenue,

        /**
         * Total revenue generated this month.
         */
        BigDecimal monthRevenue,

        /**
         * Number of orders placed today.
         */
        long todayOrders,

        /**
         * Number of orders placed this week.
         */
        long weekOrders,

        /**
         * Number of orders placed this month.
         */
        long monthOrders
) {
}
