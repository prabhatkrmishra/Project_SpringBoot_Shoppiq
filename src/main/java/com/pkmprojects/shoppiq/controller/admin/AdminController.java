package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.dto.admin.analytics.*;
import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.request.BulkItemRequest;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.business.AdminCannotBlockSelfException;
import com.pkmprojects.shoppiq.service.ItemService;
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
 * REST controller for admin dashboard and management operations.
 *
 * <p>
 * Exposes all administrative endpoints under {@code /admin/**}.
 * All endpoints require {@code ADMIN} role.
 * </p>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>Dashboard: Summary, Sales Analytics, Recent Activity</li>
 *     <li>Inventory: List, Low Stock, Out of Stock, Adjust, Bulk Adjust</li>
 *     <li>Orders: List, Get by ID, Update Status</li>
 *     <li>Users: List, Get by ID, Block, Unblock, Stats</li>
 *     <li>Payments: List, Get by ID, Refund, Stats</li>
 *     <li>Reviews: List, Delete</li>
 *     <li>Reports: Sales, Revenue, Product, Customer, Inventory, Export</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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

    public AdminController(AdminDashboardService dashboardService,
                           AdminInventoryService inventoryService,
                           AdminOrderService orderService,
                           AdminUserService userService,
                           AdminPaymentService paymentService,
                           AdminReviewService reviewService,
                           AdminReportService reportService,
                           ItemService itemService) {
        this.dashboardService = dashboardService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.userService = userService;
        this.paymentService = paymentService;
        this.reviewService = reviewService;
        this.reportService = reportService;
        this.itemService = itemService;
    }

    // =========================================================
    // Dashboard
    // =========================================================

    @GetMapping("/dashboard/summary")
    public DashboardSummaryResponse getDashboardSummary() {
        return dashboardService.getDashboardSummary();
    }

    @GetMapping("/dashboard/sales-analytics")
    public SalesAnalyticsResponse getSalesAnalytics() {
        return dashboardService.getSalesAnalytics();
    }

    @GetMapping("/dashboard/recent-activity")
    public RecentActivityResponse getRecentActivity() {
        return dashboardService.getRecentActivity();
    }

    // =========================================================
    // Items (Bulk)
    // =========================================================

    @PostMapping("/items/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ItemResponse> createBulkItems(
            @Valid @RequestBody BulkItemRequest request
    ) {
        return itemService.createBulk(request.items());
    }

    // =========================================================
    // Inventory
    // =========================================================

    @GetMapping("/inventory")
    public List<AdminProductInventoryResponse> getAllInventory() {
        return inventoryService.getAllProductInventory();
    }

    @GetMapping("/inventory/low-stock")
    public List<AdminProductInventoryResponse> getLowStockProducts() {
        return inventoryService.getLowStockProducts();
    }

    @GetMapping("/inventory/out-of-stock")
    public List<AdminProductInventoryResponse> getOutOfStockProducts() {
        return inventoryService.getOutOfStockProducts();
    }

    @PutMapping("/inventory/{itemId}")
    public AdminProductInventoryResponse adjustStock(
            @PathVariable @Min(1) Long itemId,
            @Valid @RequestBody StockAdjustmentRequest request
    ) {
        return inventoryService.adjustStock(itemId, request);
    }

    @PostMapping("/inventory/bulk-adjust")
    public List<AdminProductInventoryResponse> bulkAdjustStock(
            @RequestBody Map<Long, @Valid StockAdjustmentRequest> adjustments
    ) {
        return inventoryService.bulkUpdateStock(adjustments);
    }

    @GetMapping("/inventory/summary")
    public AdminInventoryService.InventoryDashboardSummary getInventorySummary() {
        return inventoryService.getInventoryDashboardSummary();
    }

    // =========================================================
    // Orders
    // =========================================================

    @GetMapping("/orders")
    public AdminOrderService.PageResponse<AdminOrderResponse> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @jakarta.validation.constraints.Max(100) int size
    ) {
        return orderService.getAllOrders(status, page, size);
    }

    @GetMapping("/orders/{orderId}")
    public AdminOrderResponse getOrderById(@PathVariable @Min(1) Long orderId) {
        return orderService.getOrderById(orderId);
    }

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

    @GetMapping("/users")
    public AdminUserService.PageResponse<AdminUserResponse> getAllCustomers(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @jakarta.validation.constraints.Max(100) int size
    ) {
        return userService.getAllCustomers(enabled, page, size);
    }

    @GetMapping("/users/{userId}")
    public AdminUserResponse getCustomerById(@PathVariable @Min(1) Long userId) {
        return userService.getCustomerById(userId);
    }

    @PutMapping("/users/{userId}/block")
    public AdminUserResponse blockCustomer(
            @PathVariable @Min(1) Long userId,
            @AuthenticationPrincipal User currentUser) {
        if (userId.equals(currentUser.getId())) {
            throw AdminCannotBlockSelfException.block();
        }
        return userService.blockCustomer(userId);
    }

    @PutMapping("/users/{userId}/unblock")
    public AdminUserResponse unblockCustomer(
            @PathVariable @Min(1) Long userId,
            @AuthenticationPrincipal User currentUser) {
        if (userId.equals(currentUser.getId())) {
            throw AdminCannotBlockSelfException.unblock();
        }
        return userService.unblockCustomer(userId);
    }

    @GetMapping("/users/stats")
    public AdminUserService.CustomerDashboardStats getCustomerStats() {
        return userService.getCustomerDashboardStats();
    }

    // =========================================================
    // Payments
    // =========================================================

    @GetMapping("/payments")
    public AdminPaymentService.PageResponse<AdminPaymentResponse> getAllPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @jakarta.validation.constraints.Max(100) int size
    ) {
        return paymentService.getAllPayments(status, page, size);
    }

    @GetMapping("/payments/{paymentId}")
    public AdminPaymentResponse getPaymentById(@PathVariable @Min(1) Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    @PutMapping("/payments/{paymentId}/refund")
    public AdminPaymentResponse refundPayment(@PathVariable @Min(1) Long paymentId) {
        return paymentService.refundPayment(paymentId);
    }

    @GetMapping("/payments/stats")
    public AdminPaymentService.PaymentDashboardStats getPaymentStats() {
        return paymentService.getPaymentDashboardStats();
    }

    // =========================================================
    // Reviews
    // =========================================================

    @GetMapping("/reviews")
    public AdminReviewService.PageResponse<AdminReviewResponse> getAllReviews(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @jakarta.validation.constraints.Max(100) int size
    ) {
        return reviewService.getAllReviews(page, size);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable @Min(1) Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reviews/{reviewId}/approve")
    public AdminReviewResponse approveReview(@PathVariable @Min(1) Long reviewId) {
        return reviewService.approveReview(reviewId);
    }

    @PutMapping("/reviews/{reviewId}/reject")
    public AdminReviewResponse rejectReview(@PathVariable @Min(1) Long reviewId) {
        return reviewService.rejectReview(reviewId);
    }

    // =========================================================
    // Reports
    // =========================================================

    @GetMapping("/reports/sales")
    public AdminReportService.SalesReport getSalesReport(
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate
    ) {
        return reportService.generateSalesReport(startDate, endDate);
    }

    @GetMapping("/reports/revenue")
    public AdminReportService.RevenueReport getRevenueReport(
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate
    ) {
        return reportService.generateRevenueReport(startDate, endDate);
    }

    @GetMapping("/reports/products")
    public AdminReportService.ProductReport getProductReport(
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate
    ) {
        return reportService.generateProductReport(startDate, endDate);
    }

    @GetMapping("/reports/customers")
    public AdminReportService.CustomerReport getCustomerReport(
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate
    ) {
        return reportService.generateCustomerReport(startDate, endDate);
    }

    @GetMapping("/reports/inventory")
    public AdminReportService.InventoryReport getInventoryReport() {
        return reportService.generateInventoryReport();
    }

    @GetMapping("/reports/commission")
    public List<CommissionReportResponse> getCommissionReport() {
        return reportService.generateCommissionReport();
    }

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