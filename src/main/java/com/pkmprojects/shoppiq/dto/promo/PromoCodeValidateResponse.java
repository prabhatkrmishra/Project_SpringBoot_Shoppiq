package com.pkmprojects.shoppiq.dto.promo;

import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.enums.DiscountType;

import java.math.BigDecimal;

/**
 * Response payload returned after validating a promo code.
 *
 * <p>This response includes both the raw discount parameters
 * ({@code discountType}, {@code discountValue}) and the computed
 * {@code discount} amount for the given cart. The frontend uses
 * this to display the discount preview before the user confirms
 * the order.</p>
 *
 * <p><b>Computed field:</b> {@code discount} is the actual monetary
 * discount that would be applied — for percentage discounts, this is
 * {@code subtotal × discountValue / 100}, capped at {@code maxDiscountAmount}.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PromoCodeValidateResponse(
        String code,
        BigDecimal discount,
        DiscountType discountType,
        BigDecimal discountValue,
        CouponType couponType
) {
}
