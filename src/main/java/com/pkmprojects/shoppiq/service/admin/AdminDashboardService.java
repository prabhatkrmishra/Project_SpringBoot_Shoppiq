package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.analytics.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;

/**
 * <strong>Spring Boot Concept:</strong> Business contract for admin dashboard analytics.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * This interface defines the <strong>Service layer</strong> contract for the admin dashboard.
 * In the layered architecture {@code AdminDashboardController → AdminDashboardService → ReadModels/Repositories}:
 * </p>
 * <ul>
 *   <li><strong>Controller</strong> maps REST endpoints and delegates to this service.</li>
 *   <li><strong>Service</strong> (this interface) defines the business operations for dashboard analytics.</li>
 *   <li><strong>ReadModels/Repositories</strong> handle data retrieval (hidden behind the implementation).</li>
 * </ul>
 *
 * <h2>Interface-Segregation Pattern</h2>
 * <p>
 * By defining a separate interface, the controller layer depends on an abstraction rather than
 * a concrete implementation. This promotes loose coupling and allows the implementation to be
 * mocked or replaced without affecting controllers.
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *     <li>Provide dashboard summary metrics (user/product/order counts, revenue).</li>
 *     <li>Provide sales analytics with time-series data (daily/weekly/monthly breakdowns).</li>
 *     <li>Provide recent activity feed (recent orders, payments, reviews, users).</li>
 * </ul>
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
