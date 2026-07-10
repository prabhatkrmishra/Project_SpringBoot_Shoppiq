package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.request.ItemRequest;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.seller.SellerProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for seller product management.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create a new product (DRAFT).</li>
 *     <li>List the authenticated seller's products.</li>
 *     <li>Retrieve a specific product by ID.</li>
 *     <li>Update the authenticated seller's product.</li>
 *     <li>Delete the authenticated seller's product.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>All endpoints require SELLER or ADMIN role.</li>
 *     <li>The authenticated user is injected via {@link AuthenticationPrincipal}.</li>
 *     <li>Ownership is enforced at the service layer.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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

    @PostMapping("/create")
    public ResponseEntity<ItemResponse> createProduct(
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal User currentUser) {
        ItemResponse response = sellerProductService.createProduct(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ItemResponse>> getMyProducts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        PageResponse<ItemResponse> products = sellerProductService.getMyProducts(currentUser, page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getMyProductById(
            @PathVariable @Positive(message = "Product id must be a positive number") Long id,
            @AuthenticationPrincipal User currentUser) {
        ItemResponse response = sellerProductService.getMyProductById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ItemResponse> updateProduct(
            @PathVariable @Positive(message = "Product id must be a positive number") Long id,
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal User currentUser) {
        ItemResponse response = sellerProductService.updateProduct(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable @Positive(message = "Product id must be a positive number") Long id,
            @AuthenticationPrincipal User currentUser) {
        sellerProductService.deleteProduct(id, currentUser);
        return ResponseEntity.ok().build();
    }
}
