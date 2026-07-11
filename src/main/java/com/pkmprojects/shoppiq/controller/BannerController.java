package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.service.BannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public REST controller for homepage banner data.
 *
 * <p>Exposes a read-only endpoint for the active banners displayed
 * on the homepage Sales &amp; Offers section.</p>
 *
 * @author PrabhatKrMishra
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
     * @return 200 OK with list of active banners
     */
    @GetMapping("/active")
    public List<BannerResponse> getActiveBanners() {
        return bannerService.findAllActive();
    }
}
