package com.pkmprojects.shoppiq.service.banner;

import com.pkmprojects.shoppiq.dto.banner.BannerRequest;
import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.banner.Banner;
import com.pkmprojects.shoppiq.exception.general.banner.BannerNotFoundException;
import com.pkmprojects.shoppiq.repository.banner.BannerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link BannerService} implementation providing public retrieval of active banners
 * and admin CRUD operations including toggling active status.
 *
 * @author prabhatkrmishra
 * @see BannerService
 * @since 1.0.0
 */
@Service
@Transactional
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    public BannerServiceImpl(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    /**
     * Returns all active banners ordered by display position.
     *
     * <p>Results are cached for 1 hour to reduce database load on the homepage.</p>
     *
     * @return list of active banner responses
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable("banners")
    public List<BannerResponse> findAllActive() {
        return bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(BannerResponse::from)
                .toList();
    }

    /**
     * Retrieves a paginated list of all banners ordered by display position.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated banner responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<BannerResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        var bannerPage = bannerRepository.findAll(pageable);
        return PageResponse.of(bannerPage, BannerResponse::from);
    }

    /**
     * Retrieves a single banner by ID.
     *
     * @param id banner ID
     * @return banner response
     * @throws BannerNotFoundException if the banner does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public BannerResponse findById(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> BannerNotFoundException.forId(id));
        return BannerResponse.from(banner);
    }

    /**
     * Creates a new banner with default values for optional fields.
     *
     * <p>Evicts the banners cache to ensure fresh data on next read.</p>
     *
     * @param request banner creation payload
     * @return created banner response
     */
    @Override
    @CacheEvict(value = "banners", allEntries = true)
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

    /**
     * Updates an existing banner with partial field-level granularity.
     *
     * <p>Evicts the banners cache to ensure fresh data on next read.</p>
     *
     * @param id      banner ID
     * @param request banner update payload
     * @return updated banner response
     * @throws BannerNotFoundException if the banner does not exist
     */
    @Override
    @CacheEvict(value = "banners", allEntries = true)
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

    /**
     * Toggles the active status of a banner using a custom repository query.
     *
     * <p>Evicts the banners cache to ensure fresh data on next read.</p>
     *
     * @param id banner ID
     * @return updated banner response with toggled active flag
     * @throws BannerNotFoundException if the banner does not exist
     */
    @Override
    @CacheEvict(value = "banners", allEntries = true)
    public BannerResponse toggleActive(Long id) {
        int updated = bannerRepository.toggleActive(id);
        if (updated == 0) {
            throw BannerNotFoundException.forId(id);
        }
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> BannerNotFoundException.forId(id));
        return BannerResponse.from(banner);
    }

    /**
     * Deletes a banner by ID.
     *
     * <p>Evicts the banners cache to ensure fresh data on next read.</p>
     *
     * @param id banner ID
     * @throws BannerNotFoundException if the banner does not exist
     */
    @Override
    @CacheEvict(value = "banners", allEntries = true)
    public void delete(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> BannerNotFoundException.forId(id));
        bannerRepository.delete(banner);
    }
}