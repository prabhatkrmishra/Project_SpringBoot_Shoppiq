package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.item.ItemRequest;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.seller.SellerProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for seller product management.
 *
 * <p>Exposes CRUD endpoints for the authenticated seller's own products. Sellers
 * can create products in DRAFT status, list their products, view individual
 * products, update product details, and delete products. Ownership enforcement
 * ensures sellers can only access their own products.</p>
 *
 * <p>This controller acts as the HTTP boundary for seller product operations. It
 * delegates all business logic — product creation, retrieval with ownership
 * checks, updates, deletion, and paginated listing — to {@link SellerProductService}.
 * The controller handles no business logic beyond page-size capping and request
 * validation.</p>
 *
 * <p>All endpoints require SELLER or ADMIN role and are mounted under
 * /seller/products.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /seller/products/create        — create a new product (DRAFT status)
 * GET    /seller/products               — paginated list of seller's products
 * GET    /seller/products/{id}          — get a single product by ID
 * PUT    /seller/products/update/{id}   — update an existing product
 * DELETE /seller/products/delete/{id}   — delete a product
 * </pre>
 *
 * @author prabhatkrmishra
 * @see SellerProductService
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/seller/products")
public class SellerProductController {

    private final SellerProductService sellerProductService;
    private final PaginationProperties pagination;

    public SellerProductController(SellerProductService sellerProductService, PaginationProperties pagination) {
        this.sellerProductService = sellerProductService;
        this.pagination = pagination;
    }

    /**
     * Creates a new product in DRAFT status for the authenticated seller.
     *
     * <p>The product is not visible on the storefront until an admin
     * publishes it.</p>
     *
     * @param request     the product creation payload (validated via @Valid)
     * @param currentUser the authenticated seller
     * @return 201 Created with the created product response
     */
    @PostMapping("/create")
    public ResponseEntity<ItemResponse> createProduct(
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        ItemResponse response = sellerProductService.createProduct(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns a paginated list of the authenticated seller's products.
     *
     * @param currentUser the authenticated seller
     * @param page        zero-based page index
     * @param size        page size (capped by the configured maximum)
     * @return 200 OK with page of product responses
     */
    @GetMapping
    public ResponseEntity<PageResponse<ItemResponse>> getMyProducts(
            @AuthenticationPrincipal(expression = "user") User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        PageResponse<ItemResponse> products = sellerProductService.getMyProducts(currentUser, page, size);
        return ResponseEntity.ok(products);
    }

    /**
     * Returns a single product by ID, ensuring it belongs to the authenticated
     * seller.
     *
     * @param id          the product ID (must be positive)
     * @param currentUser the authenticated seller
     * @return 200 OK with the product response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getMyProductById(
            @PathVariable @Positive(message = "Product id must be a positive number") Long id,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        ItemResponse response = sellerProductService.getMyProductById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing product owned by the authenticated seller.
     *
     * @param id          the product ID (must be positive)
     * @param request     the updated product payload (validated via @Valid)
     * @param currentUser the authenticated seller
     * @return 200 OK with the updated product response
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ItemResponse> updateProduct(
            @PathVariable @Positive(message = "Product id must be a positive number") Long id,
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        ItemResponse response = sellerProductService.updateProduct(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a product owned by the authenticated seller.
     *
     * <p>This action permanently removes the product. Consider the impact
     * on existing orders before deleting.</p>
     *
     * @param id          the product ID (must be positive)
     * @param currentUser the authenticated seller
     * @return 200 OK
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable @Positive(message = "Product id must be a positive number") Long id,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        sellerProductService.deleteProduct(id, currentUser);
        return ResponseEntity.ok().build();
    }
}