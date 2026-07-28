package com.pkmprojects.shoppiq.enums;

/**
 * <strong>Spring Boot Concept:</strong> Defines the cart composition constraint for a promo code.
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
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Business rule enum</strong> — Encodes a cart composition
 *         constraint as an enum to keep the domain model expressive and
 *         type-safe.</li>
 *     <li><strong>Nullable constraint in entity</strong> — When used with
 *         {@code PromoCode.couponType}, {@code null} means "no restriction,"
 *         demonstrating how optional enum fields can represent flexible
 *         business rules.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
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
