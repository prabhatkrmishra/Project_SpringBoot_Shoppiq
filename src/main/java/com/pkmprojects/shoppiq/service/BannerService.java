package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.banner.BannerRequest;
import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;

import java.util.List;

/**
 * Contract for homepage banner management.
 *
 * <p>Provides read operations for the public homepage and full CRUD
 * for the admin panel.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface BannerService {

    /**
     * Returns all active banners sorted by display order.
     *
     * <p>Used by the homepage to render the Sales &amp; Offers section.</p>
     *
     * @return ordered list of active banner responses
     */
    List<BannerResponse> findAllActive();

    /**
     * Returns all banners, paginated and sorted by display order.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated banner responses
     */
    PageResponse<BannerResponse> findAll(int page, int size);

    /**
     * Returns a single banner by ID.
     *
     * @param id banner ID
     * @return the banner response
     */
    BannerResponse findById(Long id);

    /**
     * Creates a new homepage banner.
     *
     * @param request banner payload
     * @return the created banner response
     */
    BannerResponse create(BannerRequest request);

    /**
     * Updates an existing homepage banner.
     *
     * @param id      banner ID
     * @param request updated banner payload
     * @return the updated banner response
     */
    BannerResponse update(Long id, BannerRequest request);

    /**
     * Toggles the active status of a banner.
     *
     * @param id banner ID
     * @return the updated banner response
     */
    BannerResponse toggleActive(Long id);

    /**
     * Deletes a homepage banner.
     *
     * @param id banner ID
     */
    void delete(Long id);
}
