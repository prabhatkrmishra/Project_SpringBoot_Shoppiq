package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.analytics.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;

/**
 * Business contract for admin dashboard analytics.
 *
 * <p>
 * Defines the operations for retrieving aggregated dashboard
 * statistics, sales analytics, and recent activity feeds.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Provide dashboard summary metrics.</li>
 *     <li>Provide sales analytics with time-series data.</li>
 *     <li>Provide recent activity feed.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Works exclusively with DTOs.</li>
 *     <li>Implemented by {@code AdminDashboardServiceImpl}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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