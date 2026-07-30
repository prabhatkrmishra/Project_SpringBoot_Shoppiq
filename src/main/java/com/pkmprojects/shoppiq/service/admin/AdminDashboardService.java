package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.DashboardSummaryResponse;
import com.pkmprojects.shoppiq.dto.admin.response.RecentActivityResponse;
import com.pkmprojects.shoppiq.dto.admin.response.SalesAnalyticsResponse;

/**
 * Business contract for admin dashboard analytics and reporting.
 *
 * <p>Defines operations for retrieving aggregated dashboard statistics,
 * sales analytics with time-series data, and recent activity feeds.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface AdminDashboardService {

    /**
     * Retrieves the dashboard summary statistics.
     *
     * @return dashboard summary response
     */
    DashboardSummaryResponse getDashboardSummary();

    /**
     * Retrieves sales analytics data.
     *
     * @return sales analytics response
     */
    SalesAnalyticsResponse getSalesAnalytics();

    /**
     * Retrieves recent activity feed.
     *
     * @return recent activity response
     */
    RecentActivityResponse getRecentActivity();
}
