package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeResponse;
import com.pkmprojects.shoppiq.service.PromoCodeService;
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
 * <p>All endpoints require {@code ADMIN} role and are mounted under
 * {@code /api/admin/promo-codes}.</p>
 *
 * @author PrabhatKrMishra
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
     * @param request promo code payload
     * @return 201 Created with the created promo code
     */
    @PostMapping
    public ResponseEntity<PromoCodeResponse> create(@Valid @RequestBody PromoCodeRequest request) {
        PromoCodeResponse response = promoCodeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns all promo codes, paginated.
     *
     * @return 200 OK with page of promo codes
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
     * @param id promo code ID
     * @return 200 OK with the promo code
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(promoCodeService.findById(id));
    }

    /**
     * Toggles the active status of a promo code.
     *
     * @param id promo code ID
     * @return 200 OK with the updated promo code
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<PromoCodeResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(promoCodeService.toggleActive(id));
    }

    /**
     * Deletes a promo code.
     *
     * @param id promo code ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promoCodeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
