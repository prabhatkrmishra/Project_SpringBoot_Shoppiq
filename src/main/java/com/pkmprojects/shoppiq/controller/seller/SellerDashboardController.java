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
 * <strong>Spring Boot Concept:</strong> REST controller for the seller dashboard.
 *
 * <p>Provides summary metrics and recent orders for the authenticated seller's
 * storefront. All endpoints require SELLER or ADMIN role.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Thin controller</strong> — no business logic; validates input and delegates to service layer.</li>
 *   <li><strong>Seller-scoped</strong> — data is filtered to the authenticated seller.</li>
 * </ul>
 * </p>
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
     * @param currentUser the authenticated seller
     * @return 200 OK with dashboard summary (total products, orders, revenue, etc.)
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