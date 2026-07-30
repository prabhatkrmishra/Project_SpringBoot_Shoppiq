package com.pkmprojects.shoppiq.controller.banner;

import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.service.banner.BannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public REST controller for homepage banner data.
 *
 * <p>Exposes a read-only endpoint for the active banners displayed on the
 * homepage Sales &amp; Offers section. This is an unauthenticated public endpoint
 * intended for the storefront. Only banners flagged as active by admins are
 * returned.</p>
 *
 * <p>This controller acts as the HTTP boundary for public banner retrieval. It
 * delegates the query for active banners to {@link BannerService}. No business
 * logic resides in the controller.</p>
 *
 * <p>No authentication is required. All endpoints are mounted under /api/banners.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /api/banners/active  — list all active banners sorted by display order
 * </pre>
 *
 * @author prabhatkrmishra
 * @see BannerService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * Returns all active banners sorted by display order.
     *
     * <p>Only banners with the active flag set to true are included.
     * Results are ordered by the displayOrder field for consistent
     * rendering on the homepage.</p>
     *
     * @return 200 OK with list of active banner responses
     */
    @GetMapping("/active")
    public List<BannerResponse> getActiveBanners() {
        return bannerService.findAllActive();
    }
}
