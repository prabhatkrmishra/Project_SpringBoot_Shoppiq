package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.banner.BannerRequest;
import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.service.banner.BannerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Admin REST controller for homepage banner management.
 *
 * <p>Provides full CRUD operations for promotional banners displayed in the
 * homepage Sales &amp; Offers section. Banners can be toggled active/inactive
 * without permanent deletion, allowing admins to temporarily hide promotions
 * and re-enable them later.</p>
 *
 * <p>This controller acts as the HTTP boundary for banner administration. It
 * delegates all business logic — persistence, validation, ordering, and active
 * status management — to {@link BannerService}. The controller itself contains
 * no business logic beyond page-size capping.</p>
 *
 * <p>All endpoints require ADMIN role and are mounted under /api/admin/banners.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /api/admin/banners              — list all banners (paginated)
 * GET    /api/admin/banners/{id}         — get a single banner by ID
 * POST   /api/admin/banners              — create a new banner
 * PUT    /api/admin/banners/{id}         — update an existing banner
 * PATCH  /api/admin/banners/{id}/toggle  — toggle active status
 * DELETE /api/admin/banners/{id}         — delete a banner permanently
 * </pre>
 *
 * @author prabhatkrmishra
 * @see BannerService
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/admin/banners")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    private final BannerService bannerService;
    private final PaginationProperties pagination;

    public AdminBannerController(BannerService bannerService, PaginationProperties pagination) {
        this.bannerService = bannerService;
        this.pagination = pagination;
    }

    /**
     * Returns all banners in a paginated response.
     *
     * @param page zero-based page index
     * @param size page size (capped by the configured maximum)
     * @return 200 OK with page of banner responses
     */
    @GetMapping
    public ResponseEntity<PageResponse<BannerResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(bannerService.findAll(page, size));
    }

    /**
     * Returns a single banner by its unique identifier.
     *
     * @param id the banner ID to retrieve
     * @return 200 OK with the banner response
     */
    @GetMapping("/{id}")
    public ResponseEntity<BannerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.findById(id));
    }

    /**
     * Creates a new homepage banner.
     *
     * @param request the banner payload (validated via @Valid)
     * @return 201 Created with the newly created banner response
     */
    @PostMapping
    public ResponseEntity<BannerResponse> create(@Valid @RequestBody BannerRequest request) {
        BannerResponse response = bannerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing homepage banner.
     *
     * @param id      the banner ID to update
     * @param request the updated banner payload (validated via @Valid)
     * @return 200 OK with the updated banner response
     */
    @PutMapping("/{id}")
    public ResponseEntity<BannerResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody BannerRequest request) {
        return ResponseEntity.ok(bannerService.update(id, request));
    }

    /**
     * Toggles the active status of a banner.
     *
     * <p>If the banner is currently active it becomes inactive, and vice
     * versa. Inactive banners are not displayed on the homepage.</p>
     *
     * @param id the banner ID to toggle
     * @return 200 OK with the updated banner response
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<BannerResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.toggleActive(id));
    }

    /**
     * Deletes a homepage banner permanently.
     *
     * <p>This action cannot be undone. Consider toggling the banner off
     * instead of deleting if you may want to reuse it later.</p>
     *
     * @param id the banner ID to delete
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
