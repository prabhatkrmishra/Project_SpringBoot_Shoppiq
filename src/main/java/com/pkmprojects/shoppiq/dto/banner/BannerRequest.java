package com.pkmprojects.shoppiq.dto.banner;

import com.pkmprojects.shoppiq.entity.enums.BannerType;
import jakarta.validation.constraints.*;

/**
 * Request payload for creating or updating a homepage banner.
 *
 * <p>This record carries all fields necessary to define a promotional
 * banner displayed on the storefront homepage. It is submitted to the
 * admin banner management endpoints and validated using Jakarta Bean
 * Validation before reaching the service layer. Banners are rendered
 * in a carousel or stacked layout, ordered by the {@code displayOrder}
 * field.</p>
 *
 * <p>The {@code badgeText} and {@code badgeType} fields define a small
 * pill-shaped label overlay (e.g. "Limited Time" in a primary color).
 * The {@code heading} and {@code bodyText} provide the main content,
 * while {@code buttonText} and {@code buttonLink} define an optional
 * call-to-action button. Color fields accept hex values for precise
 * brand matching.</p>
 *
 * @param badgeText    badge pill label text, required, max 50 characters;
 *                     displayed as a small overlay on the banner
 * @param badgeType    visual style variant for the badge pill
 *                     (PRIMARY, SECONDARY, ACCENT); controls the badge color
 * @param heading      main heading text of the banner, required, max 100
 *                     characters; typically the primary marketing message
 * @param bodyText     optional description text below the heading,
 *                     max 255 characters; provides additional context
 * @param buttonText   optional CTA button label, max 50 characters;
 *                     when provided, a button is rendered on the banner
 * @param buttonLink   optional CTA button URL, max 500 characters;
 *                     must be a relative path or http/https URL
 * @param headingColor hex color for the heading text, max 7 characters;
 *                     must be a valid 3 or 6 digit hex color (e.g. #FFF)
 * @param bodyColor    color for the body text, max 30 characters;
 *                     must be a valid hex or rgba value
 * @param displayOrder sort order on the homepage; lower values appear
 *                     first; must be zero or non-negative
 * @param active       whether this banner should be displayed on the homepage;
 *                     inactive banners are hidden from the storefront
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BannerRequest(

        /**
         * Badge pill label. Must not be blank. Max 50 characters.
         */
        @NotBlank(message = "Badge text is required.")
        @Size(max = 50, message = "Badge text cannot exceed 50 characters.")
        String badgeText,

        /**
         * Visual style variant. Must not be null.
         */
        @NotNull(message = "Badge type is required.")
        BannerType badgeType,

        /**
         * Main heading text. Must not be blank. Max 100 characters.
         */
        @NotBlank(message = "Heading is required.")
        @Size(max = 100, message = "Heading cannot exceed 100 characters.")
        String heading,

        /**
         * Optional description text. Max 255 characters.
         */
        @Size(max = 255, message = "Body text cannot exceed 255 characters.")
        String bodyText,

        /**
         * Optional CTA button label. Max 50 characters.
         */
        @Size(max = 50, message = "Button text cannot exceed 50 characters.")
        String buttonText,

        /**
         * Optional CTA button URL. Max 500 characters.
         */
        @Size(max = 500, message = "Button link cannot exceed 500 characters.")
        @Pattern(regexp = "^(https?://.*|/[^\\s]*)?$",
                message = "Button link must be a relative path or http/https URL.")
        String buttonLink,

        /**
         * Hex color for heading. Max 7 characters. Must be valid hex.
         */
        @Size(max = 7, message = "Heading color cannot exceed 7 characters.")
        @Pattern(regexp = "^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$",
                message = "Heading color must be a valid hex color (e.g. #FFF or #FFFFFF).")
        String headingColor,

        /**
         * Color for body text. Max 30 characters. Must be valid hex or rgba.
         */
        @Size(max = 30, message = "Body color cannot exceed 30 characters.")
        @Pattern(regexp = "^(#[0-9A-Fa-f]{3,6}|rgba?\\(\\d{1,3},\\s*\\d{1,3},\\s*\\d{1,3}(,\\s*[\\d.]+)?\\))$",
                message = "Body color must be a valid hex or rgba value.")
        String bodyColor,

        /**
         * Sort order on homepage. Must be zero or positive.
         */
        @PositiveOrZero(message = "Display order cannot be negative.")
        Integer displayOrder,

        /**
         * Whether to display on homepage.
         */
        Boolean active
) {
}
