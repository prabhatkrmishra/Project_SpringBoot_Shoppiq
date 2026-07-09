package com.pkmprojects.shoppiq.enums;

/**
 * Defines how a promo code discount is calculated.
 *
 * @author PrabhatKrMishra
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
