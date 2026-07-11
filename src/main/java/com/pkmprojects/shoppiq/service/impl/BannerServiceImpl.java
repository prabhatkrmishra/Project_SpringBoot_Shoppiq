package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.banner.BannerRequest;
import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.Banner;
import com.pkmprojects.shoppiq.exception.BannerNotFoundException;
import com.pkmprojects.shoppiq.repository.BannerRepository;
import com.pkmprojects.shoppiq.service.BannerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles homepage banner CRUD and retrieval.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    public BannerServiceImpl(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> findAllActive() {
        return bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(BannerResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BannerResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("displayOrder").ascending());
        var bannerPage = bannerRepository.findAll(pageable);
        return PageResponse.of(bannerPage, BannerResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public BannerResponse findById(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> BannerNotFoundException.forId(id));
        return BannerResponse.from(banner);
    }

    @Override
    public BannerResponse create(BannerRequest request) {
        Banner banner = Banner.builder()
                .badgeText(request.badgeText())
                .badgeType(request.badgeType())
                .heading(request.heading())
                .bodyText(request.bodyText())
                .buttonText(request.buttonText())
                .buttonLink(request.buttonLink())
                .headingColor(request.headingColor() != null ? request.headingColor() : "#FFFFFF")
                .bodyColor(request.bodyColor() != null ? request.bodyColor() : "rgba(255,255,255,0.85)")
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .active(request.active() != null ? request.active() : true)
                .build();

        banner = bannerRepository.save(banner);
        return BannerResponse.from(banner);
    }

    @Override
    public BannerResponse update(Long id, BannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> BannerNotFoundException.forId(id));

        banner.setBadgeText(request.badgeText());
        banner.setBadgeType(request.badgeType());
        banner.setHeading(request.heading());
        banner.setBodyText(request.bodyText());
        banner.setButtonText(request.buttonText());
        banner.setButtonLink(request.buttonLink());

        if (request.headingColor() != null) {
            banner.setHeadingColor(request.headingColor());
        }
        if (request.bodyColor() != null) {
            banner.setBodyColor(request.bodyColor());
        }
        if (request.displayOrder() != null) {
            banner.setDisplayOrder(request.displayOrder());
        }
        if (request.active() != null) {
            banner.setActive(request.active());
        }

        banner = bannerRepository.save(banner);
        return BannerResponse.from(banner);
    }

    @Override
    public BannerResponse toggleActive(Long id) {
        int updated = bannerRepository.toggleActive(id);
        if (updated == 0) {
            throw BannerNotFoundException.forId(id);
        }
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> BannerNotFoundException.forId(id));
        return BannerResponse.from(banner);
    }

    @Override
    public void delete(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> BannerNotFoundException.forId(id));
        bannerRepository.delete(banner);
    }
}
