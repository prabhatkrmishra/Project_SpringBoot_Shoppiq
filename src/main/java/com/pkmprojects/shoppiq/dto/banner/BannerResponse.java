package com.pkmprojects.shoppiq.dto.banner;

import com.pkmprojects.shoppiq.entity.banner.Banner;
import com.pkmprojects.shoppiq.entity.enums.BannerType;

import java.time.Instant;

/**
 * Homepage banner detail response.
 *
 * <p>This Java record is the read-only counterpart of {@link BannerRequest}.
 * It adds auto-generated server fields ({@code id}, {@code createdAt},
 * {@code updatedAt}) not present in the request.</p>
 *
 * <p><b>Mapping pattern:</b> The static {@link #from(com.pkmprojects.shoppiq.entity.banner.Banner) from()}
 * method converts the JPA entity to this DTO. This is a standard <i>entity-to-DTO</i>
 * mapping pattern that decouples the API contract from the persistence layer.</p>
 *
 * @author prabhatkrmishra
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
