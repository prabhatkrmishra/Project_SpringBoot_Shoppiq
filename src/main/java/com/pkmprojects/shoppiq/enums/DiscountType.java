package com.pkmprojects.shoppiq.enums;

/**
 * <strong>Spring Boot Concept:</strong> Defines how a promo code discount is calculated.
 *
 * <p>Used by {@link com.pkmprojects.shoppiq.entity.promo.PromoCode} to
 * determine whether the discount is a percentage of the subtotal or a
 * fixed monetary amount.</p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Strategy identifier</strong> — The checkout service uses
 *         this enum to select the discount calculation strategy
 *         (percentage vs. fixed amount). This is a form of the Strategy
 *         pattern implemented via enums and switch/if-else logic.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the JPA entity for
 *         readable database values and schema self-documentation.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum DiscountType {

    /**
     * Discount is a percentage of the order subtotal.
     */
    PERCENTAGE,

    /**
     * Discount is a fixed monetary amount subtracted from the subtotal.
     */
    FIXED_AMOUNT
}
