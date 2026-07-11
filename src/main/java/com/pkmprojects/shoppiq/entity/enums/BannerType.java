package com.pkmprojects.shoppiq.entity.enums;

/**
 * Defines the visual style variant for a homepage banner.
 *
 * <p>Each type maps to a CSS gradient class on the frontend:</p>
 * <ul>
 *     <li>{@link #PRIMARY} &rarr; {@code offer-banner-primary} (red gradient)</li>
 *     <li>{@link #SECONDARY} &rarr; {@code offer-banner-secondary} (purple gradient)</li>
 *     <li>{@link #ACCENT} &rarr; {@code offer-banner-accent} (green gradient)</li>
 *     <li>{@link #HIGHLIGHT} &rarr; {@code offer-banner-highlight} (orange/amber gradient)</li>
 *     <li>{@link #INFO} &rarr; {@code offer-banner-info} (blue gradient)</li>
 *     <li>{@link #PREMIUM} &rarr; {@code offer-banner-premium} (gold/dark gradient)</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public enum BannerType {

    /** Red gradient banner (e.g. limited-time sales). */
    PRIMARY,

    /** Purple gradient banner (e.g. new arrivals). */
    SECONDARY,

    /** Green gradient banner (e.g. perks, free shipping). */
    ACCENT,

    /** Orange/amber gradient banner (e.g. flash sale, last chance). */
    HIGHLIGHT,

    /** Blue gradient banner (e.g. announcements, general info). */
    INFO,

    /** Gold/dark gradient banner (e.g. VIP, exclusive offers). */
    PREMIUM
}
