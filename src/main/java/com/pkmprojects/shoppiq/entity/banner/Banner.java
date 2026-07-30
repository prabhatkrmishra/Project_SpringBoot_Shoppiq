package com.pkmprojects.shoppiq.entity.banner;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.enums.BannerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Represents a CMS-managed promotional banner displayed on the homepage.
 *
 * <p>Banners are rendered in the Sales &amp; Offers section and can be
 * created, edited, reordered, toggled, and deleted by administrators.
 * Each banner defines a badge label, heading, body text, optional CTA
 * button, visual style via {@link BannerType}, and display order. Banners
 * support flexible color customization for heading and body text to
 * accommodate diverse visual designs.</p>
 *
 * <p>The {@code active} flag controls whether a banner is currently
 * displayed on the homepage, allowing administrators to temporarily hide
 * banners without deleting them. The {@code displayOrder} field controls
 * the rendering sequence, with lower values appearing first. Banners
 * with the same display order are sorted by creation date.</p>
 *
 * @author prabhatkrmishra
 * @see BannerType
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
     * Badge pill label displayed on the banner to indicate its category
     * or promotional context (e.g. "Limited Time", "Just In", "Perks").
     *
     * <p>Required field with a maximum length of 50 characters. This text
     * appears in a small badge element above the heading to provide
     * quick visual context about the banner's purpose.</p>
     */
    @NotBlank(message = "Badge text is required.")
    @Size(max = 50, message = "Badge text cannot exceed 50 characters.")
    @Column(name = "badge_text", nullable = false, length = 50)
    private String badgeText;

    /**
     * Visual style variant determining the gradient background color
     * scheme applied to the banner.
     *
     * <p>Required field. Maps to a {@link BannerType} enum value that
     * corresponds to a CSS gradient class on the frontend (e.g.
     * {@code PRIMARY} for red, {@code SECONDARY} for purple). The
     * badge type determines the overall visual tone and emotional
     * context of the banner.</p>
     */
    @NotNull(message = "Badge type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false, length = 20)
    private BannerType badgeType;

    /**
     * Main heading displayed prominently on the banner (e.g. "Up to
     * 50% Off", "New Season Collection").
     *
     * <p>Required field with a maximum length of 100 characters. This
     * is the primary visual text element and should be concise and
     * attention-grabbing. The heading color is controlled by the
     * {@code headingColor} field.</p>
     */
    @NotBlank(message = "Heading is required.")
    @Size(max = 100, message = "Heading cannot exceed 100 characters.")
    @Column(nullable = false, length = 100)
    private String heading;

    /**
     * Optional descriptive text displayed below the heading to provide
     * additional context or promotional details.
     *
     * <p>Maximum length of 255 characters. When present, appears as a
     * secondary text element below the heading. The body text color is
     * controlled by the {@code bodyColor} field. May be {@code null}
     * for minimal banner designs that rely solely on the heading.</p>
     */
    @Size(max = 255, message = "Body text cannot exceed 255 characters.")
    @Column(name = "body_text", length = 255)
    private String bodyText;

    /**
     * Optional call-to-action button label (e.g. "Shop Sale", "Explore",
     * "Learn More").
     *
     * <p>Maximum length of 50 characters. When present, a clickable
     * button is rendered on the banner using this text. When {@code null},
     * no button is rendered and the banner is non-clickable. The button
     * link destination is specified by the {@code buttonLink} field.</p>
     */
    @Size(max = 50, message = "Button text cannot exceed 50 characters.")
    @Column(name = "button_text", length = 50)
    private String buttonText;

    /**
     * Optional call-to-action button URL that defines the navigation
     * destination when the banner button is clicked (e.g. "/sale",
     * "/new-arrivals", "/category/electronics").
     *
     * <p>Maximum length of 500 characters. When {@code null}, the banner
     * is non-clickable even if {@code buttonText} is present. Supports
     * both internal paths and external URLs for flexible promotional
     * routing.</p>
     */
    @Size(max = 500, message = "Button link cannot exceed 500 characters.")
    @Column(name = "button_link", length = 500)
    private String buttonLink;

    /**
     * Hex color code for the heading text (e.g. "#FFFFFF" for white,
     * "#000000" for black).
     *
     * <p>Required field with a maximum length of 7 characters. Controls
     * the visual appearance of the main heading on the banner. Defaults
     * to white ({@code #FFFFFF}) which provides good contrast against
     * the gradient backgrounds defined by {@link BannerType}.</p>
     */
    @Size(max = 7, message = "Heading color cannot exceed 7 characters.")
    @Column(name = "heading_color", nullable = false, length = 7)
    @Builder.Default
    private String headingColor = "#FFFFFF";

    /**
     * Color for the body text, specified as either a hex code or rgba
     * value (e.g. "rgba(255,255,255,0.85)" for semi-transparent white).
     *
     * <p>Required field with a maximum length of 30 characters. Controls
     * the visual appearance of the descriptive text below the heading.
     * Defaults to semi-transparent white ({@code rgba(255,255,255,0.85)})
     * for subtle contrast against the gradient background.</p>
     */
    @Size(max = 30, message = "Body color cannot exceed 30 characters.")
    @Column(name = "body_color", nullable = false, length = 30)
    @Builder.Default
    private String bodyColor = "rgba(255,255,255,0.85)";

    /**
     * Controls the display order of this banner on the homepage.
     *
     * <p>Lower values appear earlier in the carousel. Banners with
     * equal display order values are sorted by creation date
     * (oldest first). Defaults to 0 for newly created banners.
     * Administrators can reorder banners by updating this field
     * through the CMS interface.</p>
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * Whether this banner is currently displayed on the homepage.
     *
     * <p>When {@code false}, the banner is hidden from customers but
     * retained in the database for future reactivation. Defaults to
     * {@code true} for newly created banners. Administrators can
     * toggle this flag to temporarily hide banners during promotional
     * transitions without deleting content.</p>
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
