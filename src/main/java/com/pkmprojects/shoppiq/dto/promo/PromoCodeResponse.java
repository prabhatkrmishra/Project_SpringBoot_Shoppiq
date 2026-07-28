package com.pkmprojects.shoppiq.dto.promo;

import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full promo code detail response.
 *
 * <p>The response DTO counterpart of {@link PromoCodeRequest}. It adds
 * server-managed fields — {@code id}, {@code usedCount}, {@code createdAt},
 * {@code updatedAt} — that are not present in the request.</p>
 *
 * <p><b>Tracking field:</b> {@code usedCount} is incremented server-side each
 * time the promo is applied to an order, enabling usage limit enforcement.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PromoCodeResponse(

        Long id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        CouponType couponType,
        Integer minItemQuantity,
        Integer usageLimit,
        Integer usedCount,
        Integer userUsageLimit,
        Instant validFrom,
        Instant validUntil,
        Boolean active,
        Instant createdAt,
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
