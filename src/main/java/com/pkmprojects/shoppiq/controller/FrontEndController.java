package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.service.banner.BannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <strong>Spring Boot Concept:</strong> Controller for server-side rendered Thymeleaf page routes.
 *
 * <p>Maps URL paths to Thymeleaf view templates for the public-facing
 * storefront, customer account pages, and admin panels. Each method
 * returns a view name resolved by Spring MVC's {@code ThymeleafViewResolver}.
 * Controllers that require model data (e.g. the home page with banners)
 * populate the {@link Model} before delegating to the view.</p>
 *
 * <p>This controller handles NO JSON responses — all REST API endpoints
 * live under {@code /api/**} in their respective dedicated controllers.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Controller
public class FrontEndController {

    private final BannerService bannerService;

    public FrontEndController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * Renders the homepage with active banners.
     *
     * @param model Spring MVC model to populate with banner data
     * @return view name "index"
     */
    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("banners", bannerService.findAllActive());
        return "index";
    }

    /**
     * Renders the login page.
     *
     * @return view name "login"
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Renders the forgot-password page.
     *
     * @return view name "forgot-password"
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    /**
     * Renders the reset-password page.
     *
     * @return view name "reset-password"
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    /**
     * Renders the user registration page.
     *
     * @return view name "register"
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * Renders the all-items product listing page.
     *
     * @return view name "allitems"
     */
    @GetMapping("/allitems")
    public String itemsPage() {
        return "allitems";
    }

    /**
     * Renders the shop page (alias for all items).
     *
     * @return view name "allitems"
     */
    @GetMapping("/shop")
    public String shopPage() {
        return "allitems";
    }

    /**
     * Renders the new-arrivals page.
     *
     * @return view name "new-arrivals"
     */
    @GetMapping("/new-arrivals")
    public String newArrivalsPage() {
        return "new-arrivals";
    }

    /**
     * Renders the sale items page.
     *
     * @return view name "sale"
     */
    @GetMapping("/sale")
    public String salePage() {
        return "sale";
    }

    /**
     * Renders the categories listing page.
     *
     * @return view name "categories"
     */
    @GetMapping("/categories")
    public String categoriesPage() {
        return "categories";
    }

    /**
     * Renders the category detail page for a given slug.
     *
     * @param slug the category URL slug
     * @return view name "category"
     */
    @GetMapping("/category/{slug}")
    public String categoryPage(@org.springframework.web.bind.annotation.PathVariable String slug) {
        return "category";
    }

    /**
     * Renders the authenticated user's profile page.
     *
     * @return view name "profile"
     */
    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    /**
     * Renders the shopping cart page.
     *
     * @return view name "cart"
     */
    @GetMapping("/cart")
    public String cartPage() {
        return "cart";
    }

    /**
     * Renders the complete-profile page for new users.
     *
     * @return view name "completeprofile"
     */
    @GetMapping("/complete-profile")
    public String completeProfilePage() {
        return "completeprofile";
    }

    /**
     * Renders the address management page.
     *
     * @return view name "address"
     */
    @GetMapping("/address")
    public String addressPage() {
        return "address";
    }

    /**
     * Renders the add-address form page.
     *
     * @return view name "address"
     */
    @GetMapping("/address/add")
    public String addressAddPage() {
        return "address";
    }

    /**
     * Renders the edit-address form page.
     *
     * @return view name "address"
     */
    @GetMapping("/address/edit/{id}")
    public String addressEditPage() {
        return "address";
    }

    /**
     * Renders the checkout page.
     *
     * @return view name "checkout"
     */
    @GetMapping("/checkout")
    public String checkoutPage() {
        return "checkout";
    }

    /**
     * Renders the orders listing page.
     *
     * @return view name "orders"
     */
    @GetMapping("/orders")
    public String ordersPage() {
        return "orders";
    }

    /**
     * Renders the single-order detail page.
     *
     * @return view name "order-detail"
     */
    @GetMapping("/order-detail")
    public String orderDetailPage() {
        return "order-detail";
    }

    /**
     * Renders the item detail page for a given slug.
     *
     * @return view name "item-detail"
     */
    @GetMapping("/item/{slug}")
    public String itemDetailPage() {
        return "item-detail";
    }

    /**
     * Renders the payment page.
     *
     * @return view name "payment"
     */
    @GetMapping("/payment")
    public String paymentPage() {
        return "payment";
    }

    /**
     * Renders the about-us page.
     *
     * @return view name "about"
     */
    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    /**
     * Renders the contact-form page.
     *
     * @return view name "contact"
     */
    @GetMapping("/contact")
    public String contactPage() {
        return "contact";
    }

    /**
     * Renders the terms-of-service page.
     *
     * @return view name "terms"
     */
    @GetMapping("/terms")
    public String termsPage() {
        return "terms";
    }

    /**
     * Renders the privacy-policy page.
     *
     * @return view name "privacy"
     */
    @GetMapping("/privacy")
    public String privacyPage() {
        return "privacy";
    }

    /**
     * Renders the admin dashboard page.
     *
     * @return view name "admin-dashboard"
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboardPage() {
        return "admin-dashboard";
    }

    /**
     * Renders the admin inventory management page.
     *
     * @return view name "admin-inventory"
     */
    @GetMapping("/admin/inventory")
    public String adminInventoryPage() {
        return "admin-inventory";
    }

    /**
     * Renders the admin orders management page.
     *
     * @return view name "admin-orders"
     */
    @GetMapping("/admin/orders")
    public String adminOrdersPage() {
        return "admin-orders";
    }

    /**
     * Renders the admin users management page.
     *
     * @return view name "admin-users"
     */
    @GetMapping("/admin/users")
    public String adminUsersPage() {
        return "admin-users";
    }

    /**
     * Renders the admin payments management page.
     *
     * @return view name "admin-payments"
     */
    @GetMapping("/admin/payments")
    public String adminPaymentsPage() {
        return "admin-payments";
    }

    /**
     * Renders the admin reviews management page.
     *
     * @return view name "admin-reviews"
     */
    @GetMapping("/admin/reviews")
    public String adminReviewsPage() {
        return "admin-reviews";
    }

    /**
     * Renders the admin reports page.
     *
     * @return view name "admin-reports"
     */
    @GetMapping("/admin/reports")
    public String adminReportsPage() {
        return "admin-reports";
    }

    /**
     * Renders the admin categories management page.
     *
     * @return view name "admin-categories"
     */
    @GetMapping("/admin/categories")
    public String adminCategoriesPage() {
        return "admin-categories";
    }

    /**
     * Renders the admin category-import page.
     *
     * @return view name "admin-category-import"
     */
    @GetMapping("/admin/categories/import")
    public String adminCategoryImportPage() {
        return "admin-category-import";
    }

    /**
     * Renders the admin sellers management page.
     *
     * @return view name "admin-sellers"
     */
    @GetMapping("/admin/sellers")
    public String adminSellersPage() {
        return "admin-sellers";
    }

    /**
     * Renders the admin pending-approvals page.
     *
     * @return view name "admin-pending"
     */
    @GetMapping("/admin/pending")
    public String adminPendingPage() {
        return "admin-pending";
    }

    /**
     * Renders the admin sale management page.
     *
     * @return view name "admin-sale"
     */
    @GetMapping("/admin/sale")
    public String adminSalePage() {
        return "admin-sale";
    }

    /**
     * Renders the admin products management page.
     *
     * @return view name "admin-products"
     */
    @GetMapping("/admin/products")
    public String adminProductsPage() {
        return "admin-products";
    }

    /**
     * Renders the admin roles management page.
     *
     * @return view name "admin-roles"
     */
    @GetMapping("/admin/roles")
    public String adminRolesPage() {
        return "admin-roles";
    }

    /**
     * Renders the admin promo-codes management page.
     *
     * @return view name "admin-promo-codes"
     */
    @GetMapping("/admin/promo-codes")
    public String adminPromoCodesPage() {
        return "admin-promo-codes";
    }

    /**
     * Renders the admin banners management page.
     *
     * @return view name "admin-banners"
     */
    @GetMapping("/admin/banners")
    public String adminBannersPage() {
        return "admin-banners";
    }

    /**
     * Renders the admin mail-composer page.
     *
     * @return view name "admin-mail"
     */
    @GetMapping("/admin/mail")
    public String adminMailPage() {
        return "admin-mail";
    }

    /**
     * Renders the admin contact messages page.
     *
     * @return view name "admin-messages"
     */
    @GetMapping("/admin/messages")
    public String adminMessagesPage() {
        return "admin-messages";
    }

    /**
     * Renders the admin AI-chat logs listing page.
     *
     * @return view name "admin-ai-chats"
     */
    @GetMapping("/admin/ai-chats")
    public String adminAiChatsPage() {
        return "admin-ai-chats";
    }

    /**
     * Renders the admin AI-chat detail page for a specific conversation.
     *
     * @return view name "admin-ai-chat-detail"
     */
    @GetMapping("/admin/ai-chats/{chatId}")
    public String adminAiChatDetailPage() {
        return "admin-ai-chat-detail";
    }

    /**
     * Renders the seller dashboard page.
     *
     * @return view name "seller/dashboard"
     */
    @GetMapping("/seller-panel/dashboard")
    public String sellerDashboardPage() {
        return "seller/dashboard";
    }

    /**
     * Renders the seller products management page.
     *
     * @return view name "seller/products"
     */
    @GetMapping("/seller-panel/products")
    public String sellerProductsPage() {
        return "seller/products";
    }

    /**
     * Renders the seller inventory management page.
     *
     * @return view name "seller/inventory"
     */
    @GetMapping("/seller-panel/inventory")
    public String sellerInventoryPage() {
        return "seller/inventory";
    }

    /**
     * Renders the seller orders listing page.
     *
     * @return view name "seller/orders"
     */
    @GetMapping("/seller-panel/orders")
    public String sellerOrdersPage() {
        return "seller/orders";
    }

    /**
     * Renders the seller order-detail page.
     *
     * @return view name "seller/order-detail"
     */
    @GetMapping("/seller-panel/order-detail")
    public String sellerOrderDetailPage() {
        return "seller/order-detail";
    }

    /**
     * Renders the seller profile page.
     *
     * @return view name "seller/profile"
     */
    @GetMapping("/seller-panel/profile")
    public String sellerProfilePage() {
        return "seller/profile";
    }

    /**
     * Renders the my-reviews page for the authenticated user.
     *
     * @return view name "my-reviews"
     */
    @GetMapping("/my-reviews")
    public String myReviewsPage() {
        return "my-reviews";
    }
}
