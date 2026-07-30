package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.cart.CartItemResponse;
import com.pkmprojects.shoppiq.dto.category.CategoryResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.review.ItemReviewResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.dto.user.UserResponse;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.admin.AdminCannotBlockSelfException;
import com.pkmprojects.shoppiq.service.admin.*;
import com.pkmprojects.shoppiq.service.category.CategoryService;
import com.pkmprojects.shoppiq.service.item.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for admin dashboard and management operations.
 *
 * <p>Exposes all administrative endpoints under /api/admin/** including dashboard
 * summaries, inventory management, order management, user management, payment
 * management, review moderation, report generation, and bulk test-data creation.
 * Requires ADMIN role.</p>
 *
 * <p>This controller acts as the HTTP boundary for all admin operations. It delegates
 * all business logic — dashboard aggregation, inventory adjustments, order lifecycle,
 * user blocking, payment refunds, review moderation, and report generation — to
 * dedicated admin services. No business logic resides in the controller layer.</p>
 *
 * <p>All endpoints require ADMIN role via method-level security. The page size for
 * paginated endpoints is capped by the configured maximum from PaginationProperties.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /api/admin/dashboard/summary               — dashboard summary metrics
 * GET    /api/admin/dashboard/sales-analytics       — sales analytics data
 * GET    /api/admin/dashboard/recent-activity        — recent platform activity
 * POST   /api/admin/test/items/bulk                 — bulk create items (test data)
 * POST   /api/admin/test/categories/bulk            — bulk create categories (test data)
 * POST   /api/admin/test/users/bulk                 — bulk create users (test data)
 * POST   /api/admin/test/addresses/bulk             — bulk create addresses (test data)
 * POST   /api/admin/test/reviews/bulk               — bulk create reviews (test data)
 * POST   /api/admin/test/sellers/bulk               — bulk create sellers (test data)
 * POST   /api/admin/test/carts/bulk                 — bulk create cart items (test data)
 * POST   /api/admin/test/orders/bulk                — bulk create orders (test data)
 * GET    /api/admin/inventory                       — paginated product inventory
 * GET    /api/admin/inventory/low-stock             — low-stock products
 * GET    /api/admin/inventory/out-of-stock          — out-of-stock products
 * PUT    /api/admin/inventory/{itemId}              — adjust stock quantity
 * POST   /api/admin/inventory/bulk-adjust           — bulk stock adjustment
 * PUT    /api/admin/inventory/{itemId}/on-sale      — toggle on-sale flag
 * PUT    /api/admin/inventory/{itemId}/discount     — update discount percentage
 * PUT    /api/admin/inventory/{itemId}/put-on-sale  — set on-sale with discount
 * PUT    /api/admin/inventory/bulk-on-sale          — bulk toggle on-sale
 * GET    /api/admin/inventory/summary               — inventory dashboard summary
 * GET    /api/admin/orders                          — paginated orders list
 * GET    /api/admin/orders/{orderId}                — single order detail
 * PUT    /api/admin/orders/{orderId}/status         — update order status
 * GET    /api/admin/users                           — paginated customer list
 * GET    /api/admin/users/{userId}                  — single customer detail
 * PUT    /api/admin/users/{userId}/block            — block a customer
 * PUT    /api/admin/users/{userId}/unblock          — unblock a customer
 * GET    /api/admin/users/stats                     — customer dashboard stats
 * GET    /api/admin/payments                        — paginated payments list
 * GET    /api/admin/payments/{paymentId}            — single payment detail
 * PUT    /api/admin/payments/{paymentId}/refund     — refund a payment
 * GET    /api/admin/payments/stats                  — payment dashboard stats
 * GET    /api/admin/reviews                         — paginated reviews list
 * DELETE /api/admin/reviews/{reviewId}              — delete a review
 * PUT    /api/admin/reviews/{reviewId}/approve      — approve a review
 * PUT    /api/admin/reviews/{reviewId}/reject       — reject a review
 * GET    /api/admin/reports/sales                   — sales report
 * GET    /api/admin/reports/revenue                 — revenue report
 * GET    /api/admin/reports/products                — product performance report
 * GET    /api/admin/reports/customers               — customer activity report
 * GET    /api/admin/reports/inventory               — inventory status report
 * GET    /api/admin/reports/commission              — seller commission report
 * GET    /api/admin/reports/export                  — export report as file
 * </pre>
 *
 * @author prabhatkrmishra
 * @see AdminDashboardService
 * @see AdminInventoryService
 * @see AdminOrderService
 * @see AdminUserService
 * @see AdminPaymentService
 * @see AdminReviewService
 * @see AdminReportService
 * @see AdminTestDataService
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminDashboardService dashboardService;
    private final AdminInventoryService inventoryService;
    private final AdminOrderService orderService;
    private final AdminUserService userService;
    private final AdminPaymentService paymentService;
    private final AdminReviewService reviewService;
    private final AdminReportService reportService;
    private final ItemService itemService;
    private final CategoryService categoryService;
    private final AdminTestDataService testDataService;
    private final PaginationProperties pagination;

    public AdminController(AdminDashboardService dashboardService,
                           AdminInventoryService inventoryService,
                           AdminOrderService orderService,
                           AdminUserService userService,
                           AdminPaymentService paymentService,
                           AdminReviewService reviewService,
                           AdminReportService reportService,
                           ItemService itemService,
                           CategoryService categoryService,
                           AdminTestDataService testDataService,
                           PaginationProperties pagination) {
        this.dashboardService = dashboardService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.userService = userService;
        this.paymentService = paymentService;
        this.reviewService = reviewService;
        this.reportService = reportService;
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.testDataService = testDataService;
        this.pagination = pagination;
    }

    // =========================================================
    // Dashboard
    // =========================================================

    /**
     * Returns the admin dashboard summary including revenue, order count,
     * user count, and product count.
     *
     * <p>Aggregates data from multiple services to provide a single
     * overview response for the admin dashboard landing page.</p>
     *
     * @return 200 OK with the dashboard summary response
     */
    @GetMapping("/dashboard/summary")
    public DashboardSummaryResponse getDashboardSummary() {
        return dashboardService.getDashboardSummary();
    }

    /**
     * Returns sales analytics data for the admin dashboard chart views.
     *
     * <p>Provides time-series data suitable for rendering sales charts
     * on the admin dashboard. Data granularity depends on the date range.</p>
     *
     * @return 200 OK with the sales analytics response
     */
    @GetMapping("/dashboard/sales-analytics")
    public SalesAnalyticsResponse getSalesAnalytics() {
        return dashboardService.getSalesAnalytics();
    }

    /**
     * Returns recent platform activity for the admin dashboard feed.
     *
     * <p>Includes events such as new orders, user registrations, product
     * submissions, and other significant platform actions.</p>
     *
     * @return 200 OK with the recent activity response
     */
    @GetMapping("/dashboard/recent-activity")
    public RecentActivityResponse getRecentActivity() {
        return dashboardService.getRecentActivity();
    }

    // =========================================================
    // Test Data (Admin Bulk Import)
    // =========================================================

    /**
     * Creates multiple items in bulk for testing or seed purposes.
     *
     * <p>This endpoint is intended for development and testing only. It
     * bypasses normal product creation workflows and creates items directly.</p>
     *
     * @param request the bulk item creation request containing item details
     * @return 201 Created with list of created items
     */
    @PostMapping("/test/items/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ItemResponse> createBulkItems(
            @Valid @RequestBody BulkAdminItemRequest request
    ) {
        return testDataService.createBulkItems(request);
    }

    /**
     * Creates multiple categories in bulk for testing or seed purposes.
     *
     * <p>This endpoint is intended for development and testing only. It
     * delegates to the category service for bulk creation.</p>
     *
     * @param request the bulk category creation request
     * @return 201 Created with list of created categories
     */
    @PostMapping("/test/categories/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CategoryResponse> createBulkCategories(
            @Valid @RequestBody BulkCategoryRequest request
    ) {
        return categoryService.createBulk(request.categories());
    }

    /**
     * Creates multiple users in bulk for testing or seed purposes.
     *
     * @param request the bulk user creation request
     * @return 201 Created with list of created users
     */
    @PostMapping("/test/users/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<UserResponse> createBulkUsers(
            @Valid @RequestBody BulkUserRequest request
    ) {
        return testDataService.createBulkUsers(request);
    }

    /**
     * Creates multiple addresses in bulk for testing or seed purposes.
     *
     * @param request the bulk address creation request
     * @return 201 Created with list of created addresses
     */
    @PostMapping("/test/addresses/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<AddressResponse> createBulkAddresses(
            @Valid @RequestBody BulkAddressRequest request
    ) {
        return testDataService.createBulkAddresses(request);
    }

    /**
     * Creates multiple reviews in bulk for testing or seed purposes.
     *
     * @param request the bulk review creation request
     * @return 201 Created with list of created reviews
     */
    @PostMapping("/test/reviews/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ItemReviewResponse> createBulkReviews(
            @Valid @RequestBody BulkReviewRequest request
    ) {
        return testDataService.createBulkReviews(request);
    }

    /**
     * Creates multiple sellers in bulk for testing or seed purposes.
     *
     * @param request the bulk seller creation request
     * @return 201 Created with list of created sellers
     */
    @PostMapping("/test/sellers/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SellerResponse> createBulkSellers(
            @Valid @RequestBody BulkSellerRequest request
    ) {
        return testDataService.createBulkSellers(request);
    }

    /**
     * Creates multiple cart items in bulk for testing or seed purposes.
     *
     * @param request the bulk cart creation request
     * @return 201 Created with list of created cart items
     */
    @PostMapping("/test/carts/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CartItemResponse> createBulkCarts(
            @Valid @RequestBody BulkCartRequest request
    ) {
        return testDataService.createBulkCartItems(request);
    }

    /**
     * Creates multiple orders in bulk for testing or seed purposes.
     *
     * @param request the bulk order creation request
     * @return 201 Created with list of created orders
     */
    @PostMapping("/test/orders/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CheckoutResponse> createBulkOrders(
            @Valid @RequestBody BulkOrderRequest request
    ) {
        return testDataService.createBulkOrders(request);
    }

    // =========================================================
    // Inventory
    // =========================================================

    /**
     * Returns a paginated list of all product inventory records.
     *
     * <p>Includes stock quantities, on-sale status, and discount information
     * for each product. Results are paginated with configurable page size.</p>
     *
     * @param page zero-based page index
     * @param size page size (capped by the configured maximum)
     * @return 200 OK with page of product inventory responses
     */
    @GetMapping("/inventory")
    public PageResponse<AdminProductInventoryResponse> getAllInventory(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        size = Math.min(size, pagination.maxPageSize());
        return inventoryService.getAllProductInventory(page, size);
    }

    /**
     * Returns products with stock quantities below the configured low-stock
     * threshold.
     *
     * <p>Useful for identifying products that need restocking attention.</p>
     *
     * @return 200 OK with list of low-stock product responses
     */
    @GetMapping("/inventory/low-stock")
    public List<AdminProductInventoryResponse> getLowStockProducts() {
        return inventoryService.getLowStockProducts();
    }

    /**
     * Returns products that are currently out of stock.
     *
     * <p>These products have zero available quantity and are not purchasable
     * by customers until restocked.</p>
     *
     * @return 200 OK with list of out-of-stock product responses
     */
    @GetMapping("/inventory/out-of-stock")
    public List<AdminProductInventoryResponse> getOutOfStockProducts() {
        return inventoryService.getOutOfStockProducts();
    }

    /**
     * Adjusts the stock quantity for a specific product.
     *
     * <p>Applies a positive or negative delta to the current stock level.
     * An audit reason is required for traceability. The resulting stock
     * quantity must not go below zero.</p>
     *
     * @param itemId  the product ID to adjust
     * @param request the stock adjustment payload (quantity delta and reason)
     * @return 200 OK with the updated product inventory response
     */
    @PutMapping("/inventory/{itemId}")
    public AdminProductInventoryResponse adjustStock(
            @PathVariable @Min(1) Long itemId,
            @Valid @RequestBody StockAdjustmentRequest request
    ) {
        return inventoryService.adjustStock(itemId, request);
    }

    /**
     * Performs bulk stock adjustments for multiple products at once.
     *
     * <p>Accepts a map of product ID to stock adjustment requests. Each
     * adjustment is applied independently; if one fails, the entire
     * operation may roll back depending on the service implementation.</p>
     *
     * @param adjustments map of product ID to stock adjustment requests
     * @return 200 OK with list of updated product inventory responses
     */
    @PostMapping("/inventory/bulk-adjust")
    public List<AdminProductInventoryResponse> bulkAdjustStock(
            @RequestBody Map<Long, @Valid StockAdjustmentRequest> adjustments
    ) {
        return inventoryService.bulkUpdateStock(adjustments);
    }

    /**
     * Toggles the on-sale flag for a product.
     *
     * <p>When toggled on, the product appears in sale sections. When
     * toggled off, it is excluded from sale listings. This does not
     * affect the discount percentage.</p>
     *
     * @param itemId  the product ID
     * @param request the toggle request containing the boolean onSale value
     * @return 200 OK with the updated product inventory response
     */
    @PutMapping("/inventory/{itemId}/on-sale")
    public AdminProductInventoryResponse toggleOnSale(
            @PathVariable @Min(1) Long itemId,
            @Valid @RequestBody ToggleOnSaleRequest request
    ) {
        return inventoryService.toggleOnSale(itemId, request.onSale());
    }

    /**
     * Updates the discount percentage for a product.
     *
     * <p>Sets the discount value used for sale pricing calculations.
     * The on-sale flag is not modified by this endpoint.</p>
     *
     * @param itemId  the product ID
     * @param request the discount update request
     * @return 200 OK with the updated product inventory response
     */
    @PutMapping("/inventory/{itemId}/discount")
    public AdminProductInventoryResponse updateDiscount(
            @PathVariable @Min(1) Long itemId,
            @Valid @RequestBody UpdateDiscountRequest request
    ) {
        return inventoryService.updateDiscount(itemId, request.discountPercentage());
    }

    /**
     * Puts a product on sale by setting both the on-sale flag and discount
     * percentage in a single operation.
     *
     * <p>Convenience endpoint that combines the toggle and discount update
     * into one call.</p>
     *
     * @param itemId  the product ID
     * @param request the discount request for the sale price
     * @return 200 OK with the updated product inventory response
     */
    @PutMapping("/inventory/{itemId}/put-on-sale")
    public AdminProductInventoryResponse putOnSale(
            @PathVariable @Min(1) Long itemId,
            @Valid @RequestBody UpdateDiscountRequest request
    ) {
        return inventoryService.putOnSale(itemId, request.discountPercentage());
    }

    /**
     * Performs bulk on-sale toggle for multiple products.
     *
     * <p>Sets the on-sale flag and optional discount percentage for all
     * specified products in a single operation.</p>
     *
     * @param request the bulk on-sale request containing item IDs, on-sale
     *                flag, and optional discount percentage
     * @return 200 OK with list of updated product inventory responses
     */
    @PutMapping("/inventory/bulk-on-sale")
    public List<AdminProductInventoryResponse> bulkToggleOnSale(
            @Valid @RequestBody BulkOnSaleRequest request
    ) {
        return inventoryService.bulkToggleOnSale(
                request.itemIds(), request.onSale(), request.discountPercentage());
    }

    /**
     * Returns a summary of inventory metrics for the dashboard.
     *
     * <p>Includes total products, total stock value, low-stock count,
     * and out-of-stock count.</p>
     *
     * @return 200 OK with inventory dashboard summary
     */
    @GetMapping("/inventory/summary")
    public AdminInventoryService.InventoryDashboardSummary getInventorySummary() {
        return inventoryService.getInventoryDashboardSummary();
    }

    // =========================================================
    // Orders
    // =========================================================

    /**
     * Returns a paginated list of all orders, optionally filtered by status.
     *
     * <p>When a status filter is provided, only orders matching that status
     * are returned. Without the filter, all orders are returned.</p>
     *
     * @param status optional order status filter
     * @param page   zero-based page index
     * @param size   page size (capped by the configured maximum)
     * @return 200 OK with page of order responses
     */
    @GetMapping("/orders")
    public PageResponse<AdminOrderResponse> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        size = Math.min(size, pagination.maxPageSize());
        return orderService.getAllOrders(status, page, size);
    }

    /**
     * Returns a single order by its ID.
     *
     * <p>Returns the full order details including line items, payment
     * status, and shipping address.</p>
     *
     * @param orderId the order ID to retrieve
     * @return 200 OK with the full order response
     */
    @GetMapping("/orders/{orderId}")
    public AdminOrderResponse getOrderById(@PathVariable @Min(1) Long orderId) {
        return orderService.getOrderById(orderId);
    }

    /**
     * Updates the status of an order.
     *
     * <p>Transitions the order to the specified status. Valid transitions
     * depend on the current status (e.g., PLACED can move to SHIPPED
     * or CANCELLED).</p>
     *
     * @param orderId the order ID to update
     * @param status  the new order status
     * @return 200 OK with the updated order response
     */
    @PutMapping("/orders/{orderId}/status")
    public AdminOrderResponse updateOrderStatus(
            @PathVariable @Min(1) Long orderId,
            @RequestParam @NotNull OrderStatus status
    ) {
        return orderService.updateOrderStatus(orderId, status);
    }

    // =========================================================
    // Users
    // =========================================================

    /**
     * Returns a paginated list of all customers, optionally filtered by
     * enabled status.
     *
     * <p>When an enabled filter is provided, only customers matching that
     * status are returned. Without the filter, all customers are returned.</p>
     *
     * @param enabled optional filter for enabled/disabled users
     * @param page    zero-based page index
     * @param size    page size (capped by the configured maximum)
     * @return 200 OK with page of user responses
     */
    @GetMapping("/users")
    public PageResponse<AdminUserResponse> getAllCustomers(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        size = Math.min(size, pagination.maxPageSize());
        return userService.getAllCustomers(enabled, page, size);
    }

    /**
     * Returns a single customer by their user ID.
     *
     * @param userId the user ID to retrieve
     * @return 200 OK with the user response
     */
    @GetMapping("/users/{userId}")
    public AdminUserResponse getCustomerById(@PathVariable @Min(1) Long userId) {
        return userService.getCustomerById(userId);
    }

    /**
     * Blocks a customer account, preventing them from logging in.
     *
     * <p>The admin cannot block their own account. An attempt to do so
     * throws AdminCannotBlockSelfException.</p>
     *
     * @param userId      the user ID to block
     * @param currentUser the currently authenticated admin
     * @return 200 OK with the updated user response
     * @throws AdminCannotBlockSelfException if the admin attempts to block themselves
     */
    @PutMapping("/users/{userId}/block")
    public AdminUserResponse blockCustomer(
            @PathVariable @Min(1) Long userId,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        if (userId.equals(currentUser.getId())) {
            throw AdminCannotBlockSelfException.block();
        }
        return userService.blockCustomer(userId);
    }

    /**
     * Unblocks a previously blocked customer account.
     *
     * <p>The admin cannot unblock their own account. An attempt to do so
     * throws AdminCannotBlockSelfException.</p>
     *
     * @param userId      the user ID to unblock
     * @param currentUser the currently authenticated admin
     * @return 200 OK with the updated user response
     * @throws AdminCannotBlockSelfException if the admin attempts to unblock themselves
     */
    @PutMapping("/users/{userId}/unblock")
    public AdminUserResponse unblockCustomer(
            @PathVariable @Min(1) Long userId,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        if (userId.equals(currentUser.getId())) {
            throw AdminCannotBlockSelfException.unblock();
        }
        return userService.unblockCustomer(userId);
    }

    /**
     * Returns customer statistics for the admin dashboard.
     *
     * <p>Includes total customers, new registrations, blocked accounts,
     * and other customer-related metrics.</p>
     *
     * @return 200 OK with customer dashboard stats
     */
    @GetMapping("/users/stats")
    public AdminUserService.CustomerDashboardStats getCustomerStats() {
        return userService.getCustomerDashboardStats();
    }

    // =========================================================
    // Payments
    // =========================================================

    /**
     * Returns a paginated list of all payments, optionally filtered by status.
     *
     * <p>When a status filter is provided, only payments matching that status
     * are returned. Without the filter, all payments are returned.</p>
     *
     * @param status optional payment status filter
     * @param page   zero-based page index
     * @param size   page size (capped by the configured maximum)
     * @return 200 OK with page of payment responses
     */
    @GetMapping("/payments")
    public PageResponse<AdminPaymentResponse> getAllPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        size = Math.min(size, pagination.maxPageSize());
        return paymentService.getAllPayments(status, page, size);
    }

    /**
     * Returns a single payment by its ID.
     *
     * @param paymentId the payment ID to retrieve
     * @return 200 OK with the payment response
     */
    @GetMapping("/payments/{paymentId}")
    public AdminPaymentResponse getPaymentById(@PathVariable @Min(1) Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    /**
     * Processes a refund for a completed payment.
     *
     * <p>Transitions the payment status to REFUNDED and triggers any
     * associated refund logic (e.g., inventory restoration).</p>
     *
     * @param paymentId the payment ID to refund
     * @return 200 OK with the updated payment response
     */
    @PutMapping("/payments/{paymentId}/refund")
    public AdminPaymentResponse refundPayment(@PathVariable @Min(1) Long paymentId) {
        return paymentService.refundPayment(paymentId);
    }

    /**
     * Returns payment statistics for the admin dashboard.
     *
     * <p>Includes total revenue, pending payments, refunded amount,
     * and other payment-related metrics.</p>
     *
     * @return 200 OK with payment dashboard stats
     */
    @GetMapping("/payments/stats")
    public AdminPaymentService.PaymentDashboardStats getPaymentStats() {
        return paymentService.getPaymentDashboardStats();
    }

    // =========================================================
    // Reviews
    // =========================================================

    /**
     * Returns a paginated list of all product reviews.
     *
     * <p>Includes reviews in all statuses (pending, approved, rejected).
     * Used for admin review moderation.</p>
     *
     * @param page zero-based page index
     * @param size page size (capped by the configured maximum)
     * @return 200 OK with page of review responses
     */
    @GetMapping("/reviews")
    public PageResponse<AdminReviewResponse> getAllReviews(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        size = Math.min(size, pagination.maxPageSize());
        return reviewService.getAllReviews(page, size);
    }

    /**
     * Deletes a product review permanently.
     *
     * <p>This action cannot be undone. The review is removed from the
     * database entirely.</p>
     *
     * @param reviewId the review ID to delete
     * @return 204 No Content
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable @Min(1) Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Approves a pending review, making it visible on the product page.
     *
     * <p>Transitions the review from PENDING to APPROVED status. The review
     * becomes visible to all users browsing the product.</p>
     *
     * @param reviewId the review ID to approve
     * @return 200 OK with the updated review response
     */
    @PutMapping("/reviews/{reviewId}/approve")
    public AdminReviewResponse approveReview(@PathVariable @Min(1) Long reviewId) {
        return reviewService.approveReview(reviewId);
    }

    /**
     * Rejects a pending review, preventing it from being displayed.
     *
     * <p>Transitions the review from PENDING to REJECTED status. The review
     * remains visible only to the author.</p>
     *
     * @param reviewId the review ID to reject
     * @return 200 OK with the updated review response
     */
    @PutMapping("/reviews/{reviewId}/reject")
    public AdminReviewResponse rejectReview(@PathVariable @Min(1) Long reviewId) {
        return reviewService.rejectReview(reviewId);
    }

    // =========================================================
    // Reports
    // =========================================================

    /**
     * Generates a sales report for the given date range.
     *
     * <p>Includes order counts, total sales volume, and breakdown by
     * category or time period.</p>
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return 200 OK with the sales report data
     */
    @GetMapping("/reports/sales")
    public AdminReportService.SalesReport getSalesReport(
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate
    ) {
        return reportService.generateSalesReport(startDate, endDate);
    }

    /**
     * Generates a revenue report for the given date range.
     *
     * <p>Includes total revenue, revenue by category, and revenue
     * trends over the specified period.</p>
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return 200 OK with the revenue report data
     */
    @GetMapping("/reports/revenue")
    public AdminReportService.RevenueReport getRevenueReport(
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate
    ) {
        return reportService.generateRevenueReport(startDate, endDate);
    }

    /**
     * Generates a product performance report for the given date range.
     *
     * <p>Includes top-selling products, view counts, conversion rates,
     * and inventory turnover metrics.</p>
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return 200 OK with the product report data
     */
    @GetMapping("/reports/products")
    public AdminReportService.ProductReport getProductReport(
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate
    ) {
        return reportService.generateProductReport(startDate, endDate);
    }

    /**
     * Generates a customer activity report for the given date range.
     *
     * <p>Includes new registrations, active users, order frequency,
     * and customer retention metrics.</p>
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return 200 OK with the customer report data
     */
    @GetMapping("/reports/customers")
    public AdminReportService.CustomerReport getCustomerReport(
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate
    ) {
        return reportService.generateCustomerReport(startDate, endDate);
    }

    /**
     * Generates an inventory status report.
     *
     * <p>Includes total stock value, stock levels by category, and
     * low-stock/out-of-stock summaries.</p>
     *
     * @return 200 OK with the inventory report data
     */
    @GetMapping("/reports/inventory")
    public AdminReportService.InventoryReport getInventoryReport() {
        return reportService.generateInventoryReport();
    }

    /**
     * Generates a seller commission report.
     *
     * <p>Lists commission amounts earned by each seller based on their
     * sales volume and the configured commission rate.</p>
     *
     * @return 200 OK with list of commission report entries
     */
    @GetMapping("/reports/commission")
    public List<CommissionReportResponse> getCommissionReport() {
        return reportService.generateCommissionReport();
    }

    /**
     * Exports a report in the requested format as a downloadable file.
     *
     * <p>Supports PDF, Excel (XLSX), and CSV export formats. The content
     * type and file extension are set based on the requested format.</p>
     *
     * @param type      the report type to export
     * @param format    the export format (PDF, EXCEL, CSV)
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return 200 OK with the export file as a byte array attachment
     */
    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam @NotNull AdminReportService.ReportType type,
            @RequestParam @NotNull AdminReportService.ExportFormat format,
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate
    ) {
        byte[] content = reportService.exportReport(type, format, startDate, endDate);

        String filename = "shoppiq-%s-report-%s-to-%s.%s".formatted(
                type.name().toLowerCase(), startDate, endDate,
                format == AdminReportService.ExportFormat.PDF ? "pdf" :
                        format == AdminReportService.ExportFormat.EXCEL ? "xlsx" : "csv"
        );

        MediaType mediaType = switch (format) {
            case PDF -> MediaType.APPLICATION_PDF;
            case EXCEL -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case CSV -> MediaType.parseMediaType("text/csv");
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(content);
    }
}