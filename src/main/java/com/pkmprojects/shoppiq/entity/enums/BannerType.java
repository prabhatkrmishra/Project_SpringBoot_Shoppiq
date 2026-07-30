package com.pkmprojects.shoppiq.entity.enums;

/**
 * Defines the visual style variant for a homepage banner.
 *
 * <p>Each type maps to a CSS gradient class on the frontend, providing
 * a consistent visual language for promotional content. The banner type
 * determines the background gradient color scheme and overall visual tone
 * of the banner, helping customers quickly identify the category or
 * urgency of the promotion at a glance.</p>
 *
 * <p>Administrators select a banner type when creating or editing
 * banners through the CMS interface. The type should align with the
 * promotional content: sale promotions typically use warm colors (PRIMARY,
 * HIGHLIGHT), while informational announcements use cooler tones (INFO,
 * PREMIUM).</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.entity.banner.Banner
 * @since 1.0.0
 */
public enum BannerType {

    /**
     * Red gradient banner conveying urgency and excitement.
     *
     * <p>Typically used for limited-time sales, clearance events, and
     * time-sensitive promotions. The red gradient creates a sense of
     * urgency that encourages immediate customer action.</p>
     */
    PRIMARY,

    /**
     * Purple gradient banner conveying creativity and luxury.
     *
     * <p>Typically used for new arrivals, exclusive collections, and
     * premium product launches. The purple gradient suggests sophistication
     * and newness, appealing to customers seeking the latest offerings.</p>
     */
    SECONDARY,

    /**
     * Green gradient banner conveying value and positive action.
     *
     * <p>Typically used for perks, free shipping offers, and positive
     * value propositions. The green gradient suggests savings and
     * beneficial offers, attracting cost-conscious customers.</p>
     */
    ACCENT,

    /**
     * Orange/amber gradient banner conveying energy and time pressure.
     *
     * <p>Typically used for flash sales, last-chance offers, and
     * high-energy promotions. The orange gradient creates a warm,
     * energetic feel that drives impulse purchases and quick
     * decision-making.</p>
     */
    HIGHLIGHT,

    /**
     * Blue gradient banner conveying trust and general information.
     *
     * <p>Typically used for announcements, general information, and
     * informational campaigns. The blue gradient establishes trust
     * and professionalism, suitable for non-promotional content
     * that requires customer attention.</p>
     */
    INFO,

    /**
     * Gold/dark gradient banner conveying exclusivity and premium
     * status.
     *
     * <p>Typically used for VIP offers, exclusive deals, and premium
     * member promotions. The gold gradient suggests luxury and
     * exclusivity, appealing to high-value customers and loyalty
     * program members.</p>
     */
    PREMIUM
}
