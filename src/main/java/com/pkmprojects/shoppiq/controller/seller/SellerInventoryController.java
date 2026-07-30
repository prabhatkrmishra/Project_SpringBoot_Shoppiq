package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerInventoryResponse;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.seller.SellerInventoryService;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for seller inventory management.
 *
 * <p>Exposes endpoints for sellers to view their full product inventory, identify
 * low-stock and out-of-stock products, and adjust stock quantities with audit
 * reasons. Inventory data is scoped to the authenticated seller — sellers can
 * only view and modify their own products.</p>
 *
 * <p>This controller acts as the HTTP boundary for inventory operations. It
 * delegates all business logic — inventory retrieval, stock-level filtering,
 * and stock adjustment with audit logging — to {@link SellerInventoryService}.
 * The controller handles no business logic beyond page-size capping and request
 * validation.</p>
 *
 * <p>All endpoints require SELLER or ADMIN role and are mounted under
 * /seller/inventory.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /seller/inventory              — paginated full inventory
 * GET    /seller/inventory/low-stock    — low-stock products
 * GET    /seller/inventory/out-of-stock — out-of-stock products
 * PUT    /seller/inventory/{id}/adjust  — adjust stock with audit reason
 * </pre>
 *
 * @author prabhatkrmishra
 * @see SellerInventoryService
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/seller/inventory")
public class SellerInventoryController {

    private final SellerInventoryService sellerInventoryService;
    private final PaginationProperties pagination;

    public SellerInventoryController(SellerInventoryService sellerInventoryService, PaginationProperties pagination) {
        this.sellerInventoryService = sellerInventoryService;
        this.pagination = pagination;
    }

    /**
     * Returns a paginated list of the seller's full inventory with stock status.
     *
     * @param currentUser the authenticated seller
     * @param page        zero-based page index
     * @param size        page size (capped by the configured maximum)
     * @return 200 OK with page of inventory responses
     */
    @GetMapping
    public ResponseEntity<PageResponse<SellerInventoryResponse>> getInventory(
            @AuthenticationPrincipal(expression = "user") User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(sellerInventoryService.getInventory(currentUser, page, size));
    }

    /**
     * Returns the seller's low-stock products (stock below threshold).
     *
     * @param currentUser the authenticated seller
     * @param page        zero-based page index
     * @param size        page size (capped by the configured maximum)
     * @return 200 OK with page of low-stock inventory responses
     */
    @GetMapping("/low-stock")
    public ResponseEntity<PageResponse<SellerInventoryResponse>> getLowStock(
            @AuthenticationPrincipal(expression = "user") User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(sellerInventoryService.getLowStockProducts(currentUser, page, size));
    }

    /**
     * Returns the seller's out-of-stock products.
     *
     * @param currentUser the authenticated seller
     * @param page        zero-based page index
     * @param size        page size (capped by the configured maximum)
     * @return 200 OK with page of out-of-stock inventory responses
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<PageResponse<SellerInventoryResponse>> getOutOfStock(
            @AuthenticationPrincipal(expression = "user") User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(sellerInventoryService.getOutOfStockProducts(currentUser, page, size));
    }

    /**
     * Adjusts the stock quantity for a seller's product with an audit reason.
     *
     * <p>The quantity is a delta (positive or negative) applied to the
     * current stock level. An audit reason is required for traceability.</p>
     *
     * @param id          the product ID (must be positive)
     * @param quantity    the quantity adjustment delta (positive or negative)
     * @param reason      the audit reason for the adjustment (max 255 characters)
     * @param currentUser the authenticated seller
     * @return 200 OK with the updated inventory response
     */
    @PutMapping("/{id}/adjust")
    public ResponseEntity<SellerInventoryResponse> adjustStock(
            @PathVariable @Positive(message = "Product id must be a positive number") Long id,
            @RequestParam @NotNull(message = "Quantity is required.") int quantity,
            @RequestParam @NotBlank(message = "Reason is required.")
            @Size(max = 255, message = "Reason cannot exceed 255 characters.") String reason,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        SellerInventoryResponse response = sellerInventoryService.adjustStock(id, quantity, reason, currentUser);
        return ResponseEntity.ok(response);
    }
}