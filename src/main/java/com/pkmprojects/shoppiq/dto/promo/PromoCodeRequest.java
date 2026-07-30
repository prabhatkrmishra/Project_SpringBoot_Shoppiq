package com.pkmprojects.shoppiq.dto.promo;

import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.enums.DiscountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request payload for creating or updating a promotional code.
 *
 * <p>This record carries all configuration fields for a promotional code,
 * including discount parameters, usage constraints, and validity
 * window. It is submitted to the admin promo code management endpoints
 * ({@code POST /api/admin/promo-codes},
 * {@code PUT /api/admin/promo-codes/{id}}) and validated using
 * extensive Jakarta Bean Validation before reaching the service
 * layer.</p>
 *
 * <p>The DTO supports two discount types: percentage-based and fixed-amount.
 * Optional constraints include minimum order amount, maximum discount cap,
 * coupon type (SINGLE or BULK), per-item quantity thresholds, and global
 * or per-user usage limits. The validity window is defined by
 * {@code validFrom} and {@code validUntil} timestamps.</p>
 *
 * @param code              unique promo code string entered by customers, required,
 *                          max 50 characters; stored in uppercase for case-insensitive
 *                          matching
 * @param description       optional human-readable description, max 255
 *                          characters; displayed to administrators for context
 * @param discountType      discount type (PERCENTAGE or FIXED_AMOUNT), required
 * @param discountValue     discount value, required; for percentage type:
 *                          0.01 to 100.00; for fixed type: the monetary
 *                          discount amount
 * @param minOrderAmount    optional minimum cart subtotal required to apply
 *                          the promo; null means no minimum
 * @param maxDiscountAmount optional cap on the maximum discount for
 *                          percentage-based codes; null means uncapped
 * @param couponType        optional coupon type constraint (SINGLE or BULK);
 *                          SINGLE requires at least one qualifying item,
 *                          BULK requires all items to qualify
 * @param minItemQuantity   optional minimum quantity per item required
 *                          for the coupon to apply; null means no threshold
 * @param usageLimit        optional global usage limit across all users;
 *                          null means unlimited
 * @param userUsageLimit    optional per-user usage limit; null means unlimited
 * @param validFrom         timestamp when the promo code becomes valid; required
 * @param validUntil        timestamp when the promo code expires; required
 * @param active            whether the promo code is active and can be applied;
 *                          inactive codes are rejected during validation
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PromoCodeRequest(

        /**
         * Unique promo code string. Must not be blank. Max 50 characters.
         */
        @NotBlank(message = "Promo code is required.")
        @Size(max = 50, message = "Promo code cannot exceed 50 characters.")
        String code,

        /**
         * Optional human-readable description. Max 255 characters.
         */
        @Size(max = 255, message = "Description cannot exceed 255 characters.")
        String description,

        /**
         * Discount type (PERCENTAGE or FIXED_AMOUNT). Must not be null.
         */
        @NotNull(message = "Discount type is required.")
        DiscountType discountType,

        /**
         * Discount value. Must be at least 0.01.
         */
        @NotNull(message = "Discount value is required.")
        @DecimalMin(value = "0.01", message = "Discount value must be at least 0.01.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal discountValue,

        /**
         * Optional minimum order subtotal.
         */
        @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal minOrderAmount,

        /**
         * Optional maximum discount cap for percentage discounts.
         */
        @DecimalMin(value = "0.00", message = "Max discount amount cannot be negative.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal maxDiscountAmount,

        /**
         * Optional coupon type (SINGLE or BULK).
         */
        CouponType couponType,

        /**
         * Optional minimum quantity per item.
         */
        @PositiveOrZero(message = "Minimum item quantity cannot be negative.")
        Integer minItemQuantity,

        /**
         * Optional global usage limit.
         */
        @PositiveOrZero(message = "Usage limit cannot be negative.")
        Integer usageLimit,

        /**
         * Optional per-user usage limit.
         */
        @PositiveOrZero(message = "User usage limit cannot be negative.")
        Integer userUsageLimit,

        /**
         * When the code becomes valid. Must not be null.
         */
        @NotNull(message = "Valid-from date is required.")
        Instant validFrom,

        /**
         * When the code expires. Must not be null.
         */
        @NotNull(message = "Valid-until date is required.")
        Instant validUntil,

        /**
         * Whether the code is active.
         */
        Boolean active
) {
}
