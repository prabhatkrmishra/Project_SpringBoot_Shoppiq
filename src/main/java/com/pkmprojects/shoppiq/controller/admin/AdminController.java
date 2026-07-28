package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.cart.CartItemResponse;
import com.pkmprojects.shoppiq.dto.category.CategoryResponse;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.dto.review.ItemReviewResponse;
import com.pkmprojects.shoppiq.dto.user.UserResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;

import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.exception.admin.AdminCannotBlockSelfException;
import com.pkmprojects.shoppiq.service.item.ItemService;
import com.pkmprojects.shoppiq.service.category.CategoryService;
import com.pkmprojects.shoppiq.service.admin.*;
import com.pkmprojects.shoppiq.dto.admin.response.CommissionReportResponse;
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
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <strong>Spring Boot Concept:</strong> REST controller for admin dashboard and management operations.
 *
 * <p>Exposes all administrative endpoints under {@code /api/admin/**} including
 * dashboard summaries, inventory management, order management, user management,
 * payment management, review moderation, report generation, and bulk test-data
 * creation. All endpoints require the {@code ADMIN} role.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Thin controller</strong> — no business logic; validates input and delegates to service layer.</li>
 *   <li><strong>Sectioned by domain</strong> — methods are grouped into Dashboard, Inventory, Orders, Users, Payments, Reviews, and Reports sections.</li>
 *   <li><strong>Self-protection</strong> — block/unblock operations check that the admin cannot target themselves.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see AdminDashboardService
 * @see AdminInventoryService
 * @see AdminOrderService
 * @see AdminUserService
 * @see AdminPaymentService
 * @see AdminReviewService
 * @see AdminReportService
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
     * Returns the admin dashboard summary including revenue, order count, user count, and product count.
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
     * @return 200 OK with the sales analytics response
     */
    @GetMapping("/dashboard/sales-analytics")
    public SalesAnalyticsResponse getSalesAnalytics() {
        return dashboardService.getSalesAnalytics();
    }

    /**
     * Returns recent platform activity for the admin dashboard feed.
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
     * Creates multiple items in bulk for testing/seed purposes.
     *
     * @param request the bulk item creation request
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
     * Creates multiple categories in bulk for testing/seed purposes.
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
     * Creates multiple users in bulk for testing/seed purposes.
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
     * Creates multiple addresses in bulk for testing/seed purposes.
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
     * Creates multiple reviews in bulk for testing/seed purposes.
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
     * Creates multiple sellers in bulk for testing/seed purposes.
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
     * Creates multiple cart items in bulk for testing/seed purposes.
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
     * Creates multiple orders in bulk for testing/seed purposes.
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
     * @param page zero-based page index
     * @param size page size (capped by {@code pagination.maxPageSize()})
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
     * Returns products with stock quantities below the low-stock threshold.
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
     * @return 200 OK with list of out-of-stock product responses
     */
    @GetMapping("/inventory/out-of-stock")
    public List<AdminProductInventoryResponse> getOutOfStockProducts() {
        return inventoryService.getOutOfStockProducts();
    }

    /**
     * Adjusts the stock quantity for a specific product.
     *
     * @param itemId  the product ID
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
     * @param itemId the product ID
     * @param body   map containing the boolean {@code onSale} value
     * @return 200 OK with the updated product inventory response
     */
    @PutMapping("/inventory/{itemId}/on-sale")
    public AdminProductInventoryResponse toggleOnSale(
            @PathVariable @Min(1) Long itemId,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean onSale = body.getOrDefault("onSale", false);
        return inventoryService.toggleOnSale(itemId, onSale);
    }

    /**
     * Updates the discount percentage for a product.
     *
     * @param itemId the product ID
     * @param body   map containing the {@code discountPercentage} value
     * @return 200 OK with the updated product inventory response
     */
    @PutMapping("/inventory/{itemId}/discount")
    public AdminProductInventoryResponse updateDiscount(
            @PathVariable @Min(1) Long itemId,
            @RequestBody Map<String, java.math.BigDecimal> body
    ) {
        java.math.BigDecimal discountPercentage = body.get("discountPercentage");
        return inventoryService.updateDiscount(itemId, discountPercentage);
    }

    /**
     * Puts a product on sale by setting both the on-sale flag and discount percentage.
     *
     * @param itemId the product ID
     * @param body   map containing the {@code discountPercentage} value
     * @return 200 OK with the updated product inventory response
     */
    @PutMapping("/inventory/{itemId}/put-on-sale")
    public AdminProductInventoryResponse putOnSale(
            @PathVariable @Min(1) Long itemId,
            @RequestBody Map<String, java.math.BigDecimal> body
    ) {
        java.math.BigDecimal discountPercentage = body.get("discountPercentage");
        return inventoryService.putOnSale(itemId, discountPercentage);
    }

    /**
     * Performs bulk on-sale toggle for multiple products.
     *
     * @param body map containing {@code itemIds} (list of IDs), {@code onSale} (boolean), and optional {@code discountPercentage}
     * @return 200 OK with list of updated product inventory responses
     */
    @PutMapping("/inventory/bulk-on-sale")
    public List<AdminProductInventoryResponse> bulkToggleOnSale(
            @RequestBody Map<String, Object> body
    ) {
        @SuppressWarnings("unchecked")
        List<Long> itemIds = ((List<Number>) body.get("itemIds")).stream()
                .map(Number::longValue)
                .toList();
        boolean onSale = (Boolean) body.getOrDefault("onSale", false);
        java.math.BigDecimal discountPercentage = body.get("discountPercentage") != null
                ? new java.math.BigDecimal(body.get("discountPercentage").toString())
                : null;
        return inventoryService.bulkToggleOnSale(itemIds, onSale, discountPercentage);
    }

    /**
     * Returns a summary of inventory metrics for the dashboard.
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
     * @param status optional order status filter
     * @param page   zero-based page index
     * @param size   page size (capped by {@code pagination.maxPageSize()})
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
     * @param orderId the order ID
     * @return 200 OK with the full order response
     */
    @GetMapping("/orders/{orderId}")
    public AdminOrderResponse getOrderById(@PathVariable @Min(1) Long orderId) {
        return orderService.getOrderById(orderId);
    }

    /**
     * Updates the status of an order.
     *
     * @param orderId the order ID
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
     * Returns a paginated list of all customers, optionally filtered by enabled status.
     *
     * @param enabled optional filter for enabled/disabled users
     * @param page    zero-based page index
     * @param size    page size (capped by {@code pagination.maxPageSize()})
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
     * @param userId the user ID
     * @return 200 OK with the user response
     */
    @GetMapping("/users/{userId}")
    public AdminUserResponse getCustomerById(@PathVariable @Min(1) Long userId) {
        return userService.getCustomerById(userId);
    }

    /**
     * Blocks a customer account, preventing them from logging in.
     *
     * @param userId      the user ID to block
     * @param currentUser the currently authenticated admin
     * @return 200 OK with the updated user response
     * @throws com.pkmprojects.shoppiq.exception.admin.AdminCannotBlockSelfException if the admin attempts to block themselves
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
     * @param userId      the user ID to unblock
     * @param currentUser the currently authenticated admin
     * @return 200 OK with the updated user response
     * @throws com.pkmprojects.shoppiq.exception.admin.AdminCannotBlockSelfException if the admin attempts to unblock themselves
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
     * @param status optional payment status filter
     * @param page   zero-based page index
     * @param size   page size (capped by {@code pagination.maxPageSize()})
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
     * @param paymentId the payment ID
     * @return 200 OK with the payment response
     */
    @GetMapping("/payments/{paymentId}")
    public AdminPaymentResponse getPaymentById(@PathVariable @Min(1) Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    /**
     * Processes a refund for a completed payment.
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
     * @param page zero-based page index
     * @param size page size (capped by {@code pagination.maxPageSize()})
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
     * Deletes a product review.
     *
     * @param reviewId the review ID
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
     * @param reviewId the review ID
     * @return 200 OK with the updated review response
     */
    @PutMapping("/reviews/{reviewId}/approve")
    public AdminReviewResponse approveReview(@PathVariable @Min(1) Long reviewId) {
        return reviewService.approveReview(reviewId);
    }

    /**
     * Rejects a pending review, preventing it from being displayed.
     *
     * @param reviewId the review ID
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
     * @return 200 OK with the inventory report data
     */
    @GetMapping("/reports/inventory")
    public AdminReportService.InventoryReport getInventoryReport() {
        return reportService.generateInventoryReport();
    }

    /**
     * Generates a seller commission report.
     *
     * @return 200 OK with list of commission report entries
     */
    @GetMapping("/reports/commission")
    public List<CommissionReportResponse> getCommissionReport() {
        return reportService.generateCommissionReport();
    }

    /**
     * Exports a report in the requested format (PDF, Excel, or CSV) as a downloadable file.
     *
     * @param type      the report type
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