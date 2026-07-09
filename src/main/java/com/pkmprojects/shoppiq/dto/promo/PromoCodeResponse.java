package com.pkmprojects.shoppiq.dto.promo;

import com.pkmprojects.shoppiq.entity.PromoCode;
import com.pkmprojects.shoppiq.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full promo code detail response.
 *
 * @author PrabhatKrMishra
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
