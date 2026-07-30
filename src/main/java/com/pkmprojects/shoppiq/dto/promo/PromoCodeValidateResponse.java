package com.pkmprojects.shoppiq.dto.promo;

import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.enums.DiscountType;

import java.math.BigDecimal;

/**
 * Response payload returned after validating a promo code.
 *
 * <p>This record provides both the raw discount parameters and the
 * computed discount amount for the given cart contents. The frontend
 * uses this response to display the discount preview on the checkout
 * page, showing the customer exactly how much they will save before
 * confirming the order.</p>
 *
 * <p>The {@code discount} field is the actual monetary amount that
 * would be deducted from the order subtotal. For percentage discounts,
 * this is computed as {@code subtotal * discountValue / 100}, capped
 * at {@code maxDiscountAmount} if one is configured. For fixed-amount
 * discounts, it is simply the {@code discountValue} itself (capped
 * at the cart subtotal to prevent negative totals).</p>
 *
 * @param code          the promo code string that was successfully validated
 * @param discount      computed monetary discount amount that would be
 *                      applied to the order
 * @param discountType  discount type (PERCENTAGE or FIXED_AMOUNT)
 * @param discountValue raw discount value (percentage or fixed amount)
 *                      as configured on the promo code
 * @param couponType    optional coupon type constraint (SINGLE or BULK)
 *                      applied to this promo code; null if unconstrained
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PromoCodeValidateResponse(
        /**
         * The promo code string that was validated.
         */
        String code,

        /**
         * Computed monetary discount amount.
         */
        BigDecimal discount,

        /**
         * Discount type (PERCENTAGE or FIXED_AMOUNT).
         */
        DiscountType discountType,

        /**
         * Raw discount value (percentage or fixed amount).
         */
        BigDecimal discountValue,

        /**
         * Optional coupon type constraint.
         */
        CouponType couponType
) {
}
