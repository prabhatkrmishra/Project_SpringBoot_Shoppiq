package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.seller.response.SellerDashboardResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.user.User;

import java.util.List;

/**
 * Business contract for the seller dashboard.
 *
 * <p>Defines operations for retrieving dashboard summary metrics and recent
 * orders for the authenticated seller's store.</p>
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
