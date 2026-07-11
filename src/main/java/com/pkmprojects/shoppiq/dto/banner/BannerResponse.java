package com.pkmprojects.shoppiq.dto.banner;

import com.pkmprojects.shoppiq.entity.Banner;
import com.pkmprojects.shoppiq.entity.enums.BannerType;

import java.time.Instant;

/**
 * Homepage banner detail response.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record BannerResponse(

        Long id,
        String badgeText,
        BannerType badgeType,
        String heading,
        String bodyText,
        String buttonText,
        String buttonLink,
        String headingColor,
        String bodyColor,
        Integer displayOrder,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Constructs a {@link BannerResponse} from a {@link Banner} entity.
     *
     * @param banner source entity
     * @return response DTO
     */
    public static BannerResponse from(Banner banner) {
        return new BannerResponse(
                banner.getId(),
                banner.getBadgeText(),
                banner.getBadgeType(),
                banner.getHeading(),
                banner.getBodyText(),
                banner.getButtonText(),
                banner.getButtonLink(),
                banner.getHeadingColor(),
                banner.getBodyColor(),
                banner.getDisplayOrder(),
                banner.isActive(),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }
}
