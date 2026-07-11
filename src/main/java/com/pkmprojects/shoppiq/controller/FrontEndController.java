package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.service.BannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontEndController {

    private final BannerService bannerService;

    public FrontEndController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("banners", bannerService.findAllActive());
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/allitems")
    public String itemsPage() {
        return "allitems";
    }

    @GetMapping("/shop")
    public String shopPage() {
        return "allitems";
    }

    @GetMapping("/new-arrivals")
    public String newArrivalsPage() {
        return "new-arrivals";
    }

    @GetMapping("/sale")
    public String salePage() {
        return "sale";
    }

    @GetMapping("/categories")
    public String categoriesPage() {
        return "categories";
    }

    @GetMapping("/category/{slug}")
    public String categoryPage() {
        return "category";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    @GetMapping("/cart")
    public String cartPage() {
        return "cart";
    }

    @GetMapping("/complete-profile")
    public String completeProfilePage() {
        return "completeprofile";
    }

    @GetMapping("/address")
    public String addressPage() {
        return "address";
    }

    @GetMapping("/address/add")
    public String addressAddPage() {
        return "address";
    }

    @GetMapping("/address/edit/{id}")
    public String addressEditPage() {
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

    @GetMapping("/item/{slug}")
    public String itemDetailPage() {
        return "item-detail";
    }

    @GetMapping("/payment")
    public String paymentPage() {
        return "payment";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "contact";
    }

    @GetMapping("/terms")
    public String termsPage() {
        return "terms";
    }

    @GetMapping("/privacy")
    public String privacyPage() {
        return "privacy";
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

    @GetMapping("/admin/categories")
    public String adminCategoriesPage() {
        return "admin-categories";
    }

    @GetMapping("/admin/categories/import")
    public String adminCategoryImportPage() {
        return "admin-category-import";
    }

    @GetMapping("/admin/sellers")
    public String adminSellersPage() {
        return "admin-sellers";
    }

    @GetMapping("/admin/pending")
    public String adminPendingPage() {
        return "admin-pending";
    }

    @GetMapping("/admin/sale")
    public String adminSalePage() {
        return "admin-sale";
    }

    @GetMapping("/admin/products")
    public String adminProductsPage() {
        return "admin-products";
    }

    @GetMapping("/admin/roles")
    public String adminRolesPage() {
        return "admin-roles";
    }

    @GetMapping("/admin/promo-codes")
    public String adminPromoCodesPage() {
        return "admin-promo-codes";
    }

    @GetMapping("/admin/banners")
    public String adminBannersPage() {
        return "admin-banners";
    }

    @GetMapping("/admin/mail")
    public String adminMailPage() {
        return "admin-mail";
    }

    @GetMapping("/admin/messages")
    public String adminMessagesPage() {
        return "admin-messages";
    }

    @GetMapping("/seller-panel/dashboard")
    public String sellerDashboardPage() {
        return "seller/dashboard";
    }

    @GetMapping("/seller-panel/products")
    public String sellerProductsPage() {
        return "seller/products";
    }

    @GetMapping("/seller-panel/inventory")
    public String sellerInventoryPage() {
        return "seller/inventory";
    }

    @GetMapping("/seller-panel/orders")
    public String sellerOrdersPage() {
        return "seller/orders";
    }

    @GetMapping("/seller-panel/order-detail")
    public String sellerOrderDetailPage() {
        return "seller/order-detail";
    }

    @GetMapping("/seller-panel/profile")
    public String sellerProfilePage() {
        return "seller/profile";
    }

    @GetMapping("/my-reviews")
    public String myReviewsPage() {
        return "my-reviews";
    }
}
