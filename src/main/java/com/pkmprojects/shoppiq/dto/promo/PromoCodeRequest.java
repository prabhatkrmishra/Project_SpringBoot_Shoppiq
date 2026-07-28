package com.pkmprojects.shoppiq.dto.promo;

import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.enums.DiscountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Request payload for creating or updating a promo code.
 *
 * <p>This Java record demonstrates <b>extensive Bean Validation</b> on a
 * complex DTO with many optional fields. Key patterns:</p>
 * <ul>
 *   <li>Optional numeric fields use {@code @DecimalMin} + {@code @Digits}
 *       for monetary precision</li>
 *   <li>Optional integer fields use {@code @PositiveOrZero}</li>
 *   <li>Enum fields like {@code discountType} and {@code couponType} are
 *       auto-validated by Spring Boot deserialization</li>
 *   <li>Date/time fields use {@code @NotNull} since {@code validFrom} and
 *       {@code validUntil} are required for the promo to be usable</li>
 * </ul>
 *
 * <p><b>API contract:</b> POST/PUT /api/admin/promo-codes — all discount
 * configuration is managed server-side through this single DTO.</p>
 *
 * @param code              unique promo code string
 * @param description       optional human-readable description
 * @param discountType      PERCENTAGE or FIXED_AMOUNT
 * @param discountValue     percentage (0.01–100.00) or fixed amount
 * @param minOrderAmount    optional minimum order subtotal
 * @param maxDiscountAmount optional cap for percentage discounts
 * @param couponType        optional SINGLE or BULK cart constraint
 * @param minItemQuantity   optional minimum unit quantity per item
 * @param usageLimit        optional global usage limit
 * @param userUsageLimit    optional per-user usage limit
 * @param validFrom         when the code becomes valid
 * @param validUntil        when the code expires
 * @param active            whether the code is active
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PromoCodeRequest(

        @NotBlank(message = "Promo code is required.")
        @Size(max = 50, message = "Promo code cannot exceed 50 characters.")
        String code,

        @Size(max = 255, message = "Description cannot exceed 255 characters.")
        String description,

        @NotNull(message = "Discount type is required.")
        DiscountType discountType,

        @NotNull(message = "Discount value is required.")
        @DecimalMin(value = "0.01", message = "Discount value must be at least 0.01.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal discountValue,

        @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal minOrderAmount,

        @DecimalMin(value = "0.00", message = "Max discount amount cannot be negative.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal maxDiscountAmount,

        CouponType couponType,

        @PositiveOrZero(message = "Minimum item quantity cannot be negative.")
        Integer minItemQuantity,

        @PositiveOrZero(message = "Usage limit cannot be negative.")
        Integer usageLimit,

        @PositiveOrZero(message = "User usage limit cannot be negative.")
        Integer userUsageLimit,

        @NotNull(message = "Valid-from date is required.")
        Instant validFrom,

        @NotNull(message = "Valid-until date is required.")
        Instant validUntil,

        Boolean active
) {
}
