package com.pkmprojects.shoppiq.entity.enums;

/**
 * <strong>Spring Boot Concept:</strong> Defines the visual style variant for a homepage banner.
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
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Frontend-backend contract</strong> — Each enum value maps
 *         to a specific CSS class in the frontend, creating a strongly-typed
 *         contract between the backend API and the UI.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the {@link Banner} entity
 *         for readable database values.</li>
 *     <li><strong>Package placement</strong> — Located in
 *         {@code entity.enums} rather than {@code enums} because this enum
 *         is specific to the Banner entity module, demonstrating how enum
 *         packages can mirror domain modules.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
