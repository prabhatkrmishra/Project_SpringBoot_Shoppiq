package com.pkmprojects.shoppiq.enums;

/**
 * Defines the cart composition constraint for a promo code.
 *
 * <p>This enum specifies the quantity requirements that a customer's cart
 * must meet to use a promo code. {@link #SINGLE} requires each cart item
 * to have a quantity of exactly 1, suitable for single-item promotions.
 * {@link #BULK} requires at least one cart item with a quantity greater
 * than 1, suitable for bulk purchase discounts.</p>
 *
 * <p>When the coupon type is null on a promo code, the cart composition
 * constraint is not enforced. This allows promo codes to be applied to
 * any cart regardless of item quantities.</p>
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
