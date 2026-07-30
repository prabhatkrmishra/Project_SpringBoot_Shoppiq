package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeResponse;
import com.pkmprojects.shoppiq.service.promo.PromoCodeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin promo code management.
 *
 * <p>Provides full CRUD operations for promotional codes that customers can
 * apply during checkout. Promo codes support discount types (percentage or
 * fixed), coupon types (order-wide or category-specific), minimum subtotal
 * requirements, and usage limits. Codes can be toggled active/inactive without
 * permanent deletion.</p>
 *
 * <p>This controller acts as the HTTP boundary for promo code administration.
 * It delegates all business logic — creation, validation, discount calculation,
 * and lifecycle management — to {@link PromoCodeService}. The controller handles
 * no business logic beyond page-size capping.</p>
 *
 * <p>All endpoints require ADMIN role and are mounted under /api/admin/promo-codes.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /api/admin/promo-codes              — create a new promo code
 * PUT    /api/admin/promo-codes/{id}         — update an existing promo code
 * GET    /api/admin/promo-codes              — list all promo codes (paginated)
 * GET    /api/admin/promo-codes/{id}         — get a single promo code
 * PATCH  /api/admin/promo-codes/{id}/toggle  — toggle active status
 * DELETE /api/admin/promo-codes/{id}         — delete a promo code permanently
 * </pre>
 *
 * @author prabhatkrmishra
 * @see PromoCodeService
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/admin/promo-codes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromoCodeController {

    private final PromoCodeService promoCodeService;
    private final PaginationProperties pagination;

    public AdminPromoCodeController(PromoCodeService promoCodeService, PaginationProperties pagination) {
        this.promoCodeService = promoCodeService;
        this.pagination = pagination;
    }

    /**
     * Creates a new promo code.
     *
     * @param request the promo code payload (validated via @Valid)
     * @return 201 Created with the created promo code response
     */
    @PostMapping
    public ResponseEntity<PromoCodeResponse> create(@Valid @RequestBody PromoCodeRequest request) {
        PromoCodeResponse response = promoCodeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing promo code.
     *
     * @param id      the promo code ID to update
     * @param request the updated promo code payload (validated via @Valid)
     * @return 200 OK with the updated promo code response
     */
    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody PromoCodeRequest request) {
        return ResponseEntity.ok(promoCodeService.update(id, request));
    }

    /**
     * Returns all promo codes in a paginated response.
     *
     * @param page zero-based page index
     * @param size page size (capped by the configured maximum)
     * @return 200 OK with page of promo code responses
     */
    @GetMapping
    public ResponseEntity<PageResponse<PromoCodeResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(promoCodeService.findAll(page, size));
    }

    /**
     * Returns a single promo code by ID.
     *
     * @param id the promo code ID to retrieve
     * @return 200 OK with the promo code response
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(promoCodeService.findById(id));
    }

    /**
     * Toggles the active status of a promo code.
     *
     * <p>Inactive promo codes cannot be applied during checkout.</p>
     *
     * @param id the promo code ID to toggle
     * @return 200 OK with the updated promo code response
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<PromoCodeResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(promoCodeService.toggleActive(id));
    }

    /**
     * Deletes a promo code permanently.
     *
     * <p>This action cannot be undone. Consider toggling the code off
     * instead if you may want to reuse it later.</p>
     *
     * @param id the promo code ID to delete
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promoCodeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
