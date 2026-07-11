package com.pkmprojects.shoppiq.dto.banner;

import com.pkmprojects.shoppiq.entity.enums.BannerType;
import jakarta.validation.constraints.*;

/**
 * Request payload for creating or updating a homepage banner.
 *
 * @param badgeText    badge pill label (e.g. "Limited Time")
 * @param badgeType    visual style variant (PRIMARY, SECONDARY, ACCENT)
 * @param heading      main heading text
 * @param bodyText     optional description text
 * @param buttonText   optional CTA button label
 * @param buttonLink   optional CTA button URL
 * @param headingColor hex color for heading (default #FFFFFF)
 * @param bodyColor    color for body text (default rgba white)
 * @param displayOrder sort order on homepage (lower = earlier)
 * @param active       whether to display on homepage
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record BannerRequest(

        @NotBlank(message = "Badge text is required.")
        @Size(max = 50, message = "Badge text cannot exceed 50 characters.")
        String badgeText,

        @NotNull(message = "Badge type is required.")
        BannerType badgeType,

        @NotBlank(message = "Heading is required.")
        @Size(max = 100, message = "Heading cannot exceed 100 characters.")
        String heading,

        @Size(max = 255, message = "Body text cannot exceed 255 characters.")
        String bodyText,

        @Size(max = 50, message = "Button text cannot exceed 50 characters.")
        String buttonText,

        @Size(max = 500, message = "Button link cannot exceed 500 characters.")
        @Pattern(regexp = "^(https?://.*|/[^\\s]*)?$",
                message = "Button link must be a relative path or http/https URL.")
        String buttonLink,

        @Size(max = 7, message = "Heading color cannot exceed 7 characters.")
        @Pattern(regexp = "^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$",
                message = "Heading color must be a valid hex color (e.g. #FFF or #FFFFFF).")
        String headingColor,

        @Size(max = 30, message = "Body color cannot exceed 30 characters.")
        @Pattern(regexp = "^(#[0-9A-Fa-f]{3,6}|rgba?\\(\\d{1,3},\\s*\\d{1,3},\\s*\\d{1,3}(,\\s*[\\d.]+)?\\))$",
                message = "Body color must be a valid hex or rgba value.")
        String bodyColor,

        @PositiveOrZero(message = "Display order cannot be negative.")
        Integer displayOrder,

        Boolean active
) {
}
