package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.seller.response.SellerDashboardResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.user.User;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Business contract for the seller dashboard.
 *
 * <p>
 * Provides summary metrics and recent activity data for the
 * authenticated seller's store.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Compute seller dashboard summary (products, orders, revenue, stock alerts).</li>
 *     <li>Retrieve recent orders containing the seller's products.</li>
 *     <li>Enforce seller-level preconditions (ACTIVE, not SUSPENDED).</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface SellerDashboardService {

    /**
     * Retrieves the dashboard summary for the authenticated seller.
     *
     * @param user the authenticated user
     * @return dashboard summary metrics
     */
    SellerDashboardResponse getDashboardSummary(User user);

    /**
     * Retrieves the most recent orders containing the seller's products.
     *
     * @param user the authenticated user
     * @return list of recent orders with seller's items
     */
    List<SellerOrderResponse> getRecentOrders(User user);
}
