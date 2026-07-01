package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.dto.seller.response.SellerDashboardResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.User;
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
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Provide dashboard summary metrics.</li>
 *     <li>Provide recent orders for the authenticated seller.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>All endpoints require SELLER or ADMIN role.</li>
 *     <li>The authenticated user is injected via {@link AuthenticationPrincipal}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@RestController
@RequestMapping("/seller/dashboard")
public class SellerDashboardController {

    private final SellerDashboardService sellerDashboardService;

    public SellerDashboardController(SellerDashboardService sellerDashboardService) {
        this.sellerDashboardService = sellerDashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<SellerDashboardResponse> getSummary(
            @AuthenticationPrincipal User currentUser) {
        SellerDashboardResponse summary = sellerDashboardService.getDashboardSummary(currentUser);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<List<SellerOrderResponse>> getRecentOrders(
            @AuthenticationPrincipal User currentUser) {
        List<SellerOrderResponse> orders = sellerDashboardService.getRecentOrders(currentUser);
        return ResponseEntity.ok(orders);
    }
}
