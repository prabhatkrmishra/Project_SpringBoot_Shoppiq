package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.dto.seller.response.SellerDashboardResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.seller.SellerDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the seller dashboard.
 *
 * <p>Provides summary metrics and recent orders for the authenticated seller's
 * storefront. The dashboard summary includes aggregate statistics such as total
 * products, total orders, revenue, and other key performance indicators. The
 * recent orders endpoint provides a quick view of the most recent orders
 * containing the seller's products.</p>
 *
 * <p>This controller acts as the HTTP boundary for dashboard queries. It delegates
 * all business logic — metric aggregation, order retrieval, and data formatting
 * — to {@link SellerDashboardService}. The controller handles no business logic
 * beyond authentication extraction.</p>
 *
 * <p>All endpoints require SELLER or ADMIN role and are mounted under
 * /seller/dashboard.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /seller/dashboard/summary        — dashboard summary metrics
 * GET    /seller/dashboard/recent-orders  — recent orders with seller products
 * </pre>
 *
 * @author prabhatkrmishra
 * @see SellerDashboardService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/seller/dashboard")
public class SellerDashboardController {

    private final SellerDashboardService sellerDashboardService;

    public SellerDashboardController(SellerDashboardService sellerDashboardService) {
        this.sellerDashboardService = sellerDashboardService;
    }

    /**
     * Returns dashboard summary metrics for the authenticated seller.
     *
     * <p>Includes aggregate statistics such as total products, total orders,
     * revenue, and other key performance indicators for the seller's storefront.</p>
     *
     * @param currentUser the authenticated seller
     * @return 200 OK with dashboard summary response
     */
    @GetMapping("/summary")
    public ResponseEntity<SellerDashboardResponse> getSummary(
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        SellerDashboardResponse summary = sellerDashboardService.getDashboardSummary(currentUser);
        return ResponseEntity.ok(summary);
    }

    /**
     * Returns the most recent orders containing the seller's products.
     *
     * <p>Provides a quick view of recent order activity for the seller's
     * storefront.</p>
     *
     * @param currentUser the authenticated seller
     * @return 200 OK with list of recent order responses
     */
    @GetMapping("/recent-orders")
    public ResponseEntity<List<SellerOrderResponse>> getRecentOrders(
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        List<SellerOrderResponse> orders = sellerDashboardService.getRecentOrders(currentUser);
        return ResponseEntity.ok(orders);
    }
}