package com.pkmprojects.shoppiq.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontEndController {
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/allitems")
    public String itemsPage() {
        return "allitems";
    }

    @GetMapping("/complete-profile")
    public String completeProfilePage() {
        return "completeprofile";
    }

    @GetMapping("/address")
    public String addressPage() {
        return "address";
    }

    @GetMapping("/checkout")
    public String checkoutPage() {
        return "checkout";
    }

    @GetMapping("/orders")
    public String ordersPage() {
        return "orders";
    }

    @GetMapping("/order-detail")
    public String orderDetailPage() {
        return "order-detail";
    }

    @GetMapping("/payment")
    public String paymentPage() {
        return "payment";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboardPage() {
        return "admin-dashboard";
    }

    @GetMapping("/admin/inventory")
    public String adminInventoryPage() {
        return "admin-inventory";
    }

    @GetMapping("/admin/orders")
    public String adminOrdersPage() {
        return "admin-orders";
    }

    @GetMapping("/admin/users")
    public String adminUsersPage() {
        return "admin-users";
    }

    @GetMapping("/admin/payments")
    public String adminPaymentsPage() {
        return "admin-payments";
    }

    @GetMapping("/admin/reviews")
    public String adminReviewsPage() {
        return "admin-reviews";
    }

    @GetMapping("/admin/reports")
    public String adminReportsPage() {
        return "admin-reports";
    }
}
