package com.pkmprojects.shoppiq.enums;

/**
 * Defines the cart composition constraint for a promo code.
 *
 * <ul>
 *     <li>{@link #SINGLE} — only valid when every cart item has
 *         {@code quantity == 1} (single-unit purchase).</li>
 *     <li>{@link #BULK} — only valid when at least one cart item has
 *         {@code quantity > 1} (multi-unit / bulk purchase).</li>
 * </ul>
 *
 * <p>When {@code null} on a promo code, the coupon type constraint is
 * not enforced and the code may be applied regardless of cart composition.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.5.0
 */
public enum CouponType {

    /**
     * Single-item coupon — each cart item must have quantity equal to 1.
     */
    SINGLE,

    /**
     * Bulk coupon — at least one cart item must have quantity greater than 1.
     */
    BULK
}
