package com.pkmprojects.shoppiq.dto.promo;

import com.pkmprojects.shoppiq.enums.DiscountType;

import java.math.BigDecimal;

/**
 * Response payload returned after validating a promo code.
 *
 * @param code           the validated promo code string
 * @param discount       the calculated discount amount for the given subtotal
 * @param discountType   PERCENTAGE or FIXED_AMOUNT
 * @param discountValue  the raw discount value (percentage or fixed amount)
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record PromoCodeValidateResponse(
        String code,
        BigDecimal discount,
        DiscountType discountType,
        BigDecimal discountValue
) {
}
