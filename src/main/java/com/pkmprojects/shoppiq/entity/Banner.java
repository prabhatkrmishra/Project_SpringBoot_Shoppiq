package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.enums.BannerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Represents a CMS-managed promotional banner displayed on the homepage.
 *
 * <p>Banners are rendered in the Sales &amp; Offers section and can be
 * created, edited, reordered, toggled, and deleted by administrators.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores badge label, heading, body text, and optional CTA button.</li>
 *     <li>Defines visual style via {@link BannerType} (gradient variant).</li>
 *     <li>Supports custom heading and body text colors.</li>
 *     <li>Controls display order and active/inactive state.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(name = "homepage_banners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Banner extends AuditableEntity {

    /**
     * Badge pill label (e.g. "Limited Time", "Just In", "Perks").
     */
    @NotBlank(message = "Badge text is required.")
    @Size(max = 50, message = "Badge text cannot exceed 50 characters.")
    @Column(name = "badge_text", nullable = false, length = 50)
    private String badgeText;

    /**
     * Visual style variant determining the gradient background.
     */
    @NotNull(message = "Badge type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false, length = 20)
    private BannerType badgeType;

    /**
     * Main heading displayed on the banner (e.g. "Up to 50% Off").
     */
    @NotBlank(message = "Heading is required.")
    @Size(max = 100, message = "Heading cannot exceed 100 characters.")
    @Column(nullable = false, length = 100)
    private String heading;

    /**
     * Optional description text below the heading.
     */
    @Size(max = 255, message = "Body text cannot exceed 255 characters.")
    @Column(name = "body_text", length = 255)
    private String bodyText;

    /**
     * Optional CTA button label (e.g. "Shop Sale", "Explore").
     *
     * <p>When {@code null}, no button is rendered.</p>
     */
    @Size(max = 50, message = "Button text cannot exceed 50 characters.")
    @Column(name = "button_text", length = 50)
    private String buttonText;

    /**
     * Optional CTA button URL (e.g. "/sale", "/new-arrivals").
     *
     * <p>When {@code null}, the banner is non-clickable.</p>
     */
    @Size(max = 500, message = "Button link cannot exceed 500 characters.")
    @Column(name = "button_link", length = 500)
    private String buttonLink;

    /**
     * Hex color for the heading text (e.g. "#FFFFFF").
     */
    @Size(max = 7, message = "Heading color cannot exceed 7 characters.")
    @Column(name = "heading_color", nullable = false, length = 7)
    @Builder.Default
    private String headingColor = "#FFFFFF";

    /**
     * Color for the body text (hex or rgba, e.g. "rgba(255,255,255,0.85)").
     */
    @Size(max = 30, message = "Body color cannot exceed 30 characters.")
    @Column(name = "body_color", nullable = false, length = 30)
    @Builder.Default
    private String bodyColor = "rgba(255,255,255,0.85)";

    /**
     * Controls the display order on the homepage (lower = earlier).
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * Whether this banner is currently displayed on the homepage.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
