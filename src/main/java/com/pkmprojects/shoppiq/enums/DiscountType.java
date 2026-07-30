package com.pkmprojects.shoppiq.enums;

/**
 * Defines how a promo code discount is calculated.
 *
 * <p>This enum determines the calculation method for promo code discounts.
 * {@link #PERCENTAGE} applies a percentage of the order subtotal, while
 * {@link #FIXED_AMOUNT} subtracts a fixed monetary amount. The discount
 * type is set when the promo code is created and cannot be changed
 * after activation.</p>
 *
 * <p>The checkout service uses this enum to determine how to apply the
 * discount to the order total. Percentage discounts scale with the order
 * size, while fixed-amount discounts provide a consistent reduction
 * regardless of order value.</p>
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
