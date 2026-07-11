package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.banner.BannerRequest;
import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.service.BannerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin homepage banner management.
 *
 * <p>All endpoints require {@code ADMIN} role and are mounted under
 * {@code /api/admin/banners}.</p>
 *
 * @author PrabhatKrMishra
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
     * Returns all banners, paginated.
     *
     * @return 200 OK with page of banners
     */
    @GetMapping
    public ResponseEntity<PageResponse<BannerResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(bannerService.findAll(page, size));
    }

    /**
     * Returns a single banner by ID.
     *
     * @param id banner ID
     * @return 200 OK with the banner
     */
    @GetMapping("/{id}")
    public ResponseEntity<BannerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.findById(id));
    }

    /**
     * Creates a new homepage banner.
     *
     * @param request banner payload
     * @return 201 Created with the created banner
     */
    @PostMapping
    public ResponseEntity<BannerResponse> create(@Valid @RequestBody BannerRequest request) {
        BannerResponse response = bannerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing homepage banner.
     *
     * @param id      banner ID
     * @param request updated banner payload
     * @return 200 OK with the updated banner
     */
    @PutMapping("/{id}")
    public ResponseEntity<BannerResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody BannerRequest request) {
        return ResponseEntity.ok(bannerService.update(id, request));
    }

    /**
     * Toggles the active status of a banner.
     *
     * @param id banner ID
     * @return 200 OK with the updated banner
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<BannerResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.toggleActive(id));
    }

    /**
     * Deletes a homepage banner.
     *
     * @param id banner ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
