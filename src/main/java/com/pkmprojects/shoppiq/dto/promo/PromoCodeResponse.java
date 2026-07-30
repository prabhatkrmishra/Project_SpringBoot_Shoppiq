package com.pkmprojects.shoppiq.dto.promo;

import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full promo code detail response for admin management and validation.
 *
 * <p>This record is the read-only counterpart of {@link PromoCodeRequest}.
 * It includes all request fields plus server-managed metadata ({@code id},
 * {@code usedCount}, {@code createdAt}, {@code updatedAt}) that are not
 * present in the request. It is returned by the admin promo code list
 * and detail endpoints and is also used in the validation response to
 * confirm the applied promo code's parameters.</p>
 *
 * <p>The {@code usedCount} field is incremented server-side each time
 * the promo is applied to a confirmed order, enabling usage limit
 * enforcement. When {@code usageLimit} is set and {@code usedCount}
 * reaches it, the promo code is no longer valid for new orders.</p>
 *
 * @param id                unique identifier of the promo code, auto-generated
 * @param code              promo code string as entered by customers (unique)
 * @param description       optional human-readable description for admin context
 * @param discountType      discount type (PERCENTAGE or FIXED_AMOUNT)
 * @param discountValue     discount value (percentage or fixed amount)
 * @param minOrderAmount    optional minimum cart subtotal required;
 *                          null means no minimum
 * @param maxDiscountAmount optional cap on percentage discount amount;
 *                          null means uncapped
 * @param couponType        optional coupon type constraint (SINGLE or BULK)
 * @param minItemQuantity   optional minimum quantity per item required
 * @param usageLimit        optional global usage limit; null means unlimited
 * @param usedCount         number of times this promo has been applied to
 *                          confirmed orders
 * @param userUsageLimit    optional per-user usage limit; null means unlimited
 * @param validFrom         timestamp when the promo code becomes valid
 * @param validUntil        timestamp when the promo code expires
 * @param active            whether the promo code is active and can be applied
 * @param createdAt         timestamp when the promo code was first created
 * @param updatedAt         timestamp of the most recent modification
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PromoCodeResponse(

        /**
         * Unique identifier of the promo code.
         */
        Long id,

        /**
         * Promo code string (unique).
         */
        String code,

        /**
         * Optional description of the promo code.
         */
        String description,

        /**
         * Discount type (PERCENTAGE or FIXED_AMOUNT).
         */
        DiscountType discountType,

        /**
         * Discount value (percentage or fixed amount).
         */
        BigDecimal discountValue,

        /**
         * Optional minimum order amount required.
         */
        BigDecimal minOrderAmount,

        /**
         * Optional maximum discount cap for percentage discounts.
         */
        BigDecimal maxDiscountAmount,

        /**
         * Optional coupon type (SINGLE or BULK).
         */
        CouponType couponType,

        /**
         * Optional minimum quantity per item.
         */
        Integer minItemQuantity,

        /**
         * Optional global usage limit. Null if unlimited.
         */
        Integer usageLimit,

        /**
         * Number of times this promo has been used.
         */
        Integer usedCount,

        /**
         * Optional per-user usage limit.
         */
        Integer userUsageLimit,

        /**
         * When the promo code becomes valid.
         */
        Instant validFrom,

        /**
         * When the promo code expires.
         */
        Instant validUntil,

        /**
         * Whether the promo code is active.
         */
        Boolean active,

        /**
         * Entity creation timestamp.
         */
        Instant createdAt,

        /**
         * Entity last update timestamp.
         */
        Instant updatedAt
) {

    /**
     * Constructs a {@link PromoCodeResponse} from a {@link PromoCode} entity.
     *
     * @param promoCode source entity
     * @return response DTO
     */
    public static PromoCodeResponse from(PromoCode promoCode) {
        return new PromoCodeResponse(
                promoCode.getId(),
                promoCode.getCode(),
                promoCode.getDescription(),
                promoCode.getDiscountType(),
                promoCode.getDiscountValue(),
                promoCode.getMinOrderAmount(),
                promoCode.getMaxDiscountAmount(),
                promoCode.getCouponType(),
                promoCode.getMinItemQuantity(),
                promoCode.getUsageLimit(),
                promoCode.getUsedCount(),
                promoCode.getUserUsageLimit(),
                promoCode.getValidFrom(),
                promoCode.getValidUntil(),
                promoCode.getActive(),
                promoCode.getCreatedAt(),
                promoCode.getUpdatedAt()
        );
    }
}
