package com.pkmprojects.shoppiq.entity.promo;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.enums.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a promotional code (coupon) that can be applied during checkout.
 *
 * <p>Defines a discount rule that customers can apply to their order subtotal.
 * Supports both percentage-based and fixed-amount discounts, with optional
 * minimum order requirements, global and per-user usage limits, and cart
 * composition rules via {@link CouponType}. Each promo code has a defined
 * validity window and can be independently activated or deactivated by
 * administrators.</p>
 *
 * <p>The code string is stored uppercase and must be globally unique.
 * The {@code usedCount} is incremented atomically at checkout time to
 * prevent race conditions under concurrent usage. Per-user usage tracking
 * is maintained through the {@link PromoCodeUsage} join entity, which
 * also prevents the same order from applying multiple promo codes.</p>
 *
 * @author prabhatkrmishra
 * @see PromoCodeUsage
 * @see com.pkmprojects.shoppiq.entity.order.Order
 * @since 1.0.0
 */
@Entity
@Table(name = "promo_codes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PromoCode extends AuditableEntity {

    /**
     * The promo code string entered by the customer at checkout.
     *
     * <p>Stored uppercase; uniqueness is enforced at the database level.
     * Required field with a maximum length of 50 characters. The code
     * is case-insensitive for customer convenience but normalized to
     * uppercase for consistent storage and lookup.</p>
     */
    @NotBlank(message = "Promo code is required.")
    @Size(max = 50, message = "Promo code cannot exceed 50 characters.")
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Human-readable description explaining the promo code's offer,
     * terms, and applicable products or categories.
     *
     * <p>Maximum length of 255 characters. Displayed to customers in
     * the checkout flow and in promotional materials. May be {@code null}
     * for codes where the discount type and value are self-explanatory.</p>
     */
    @Size(max = 255, message = "Description cannot exceed 255 characters.")
    @Column(length = 255)
    private String description;

    /**
     * How the discount value is interpreted: either as a percentage
     * of the subtotal or as a fixed monetary amount.
     *
     * <p>Required field stored as a string enum with a maximum length
     * of 20 characters. {@link DiscountType#PERCENTAGE} applies the
     * discount value as a percentage off the subtotal, while
     * {@link DiscountType#FIXED_AMOUNT} subtracts the discount value
     * directly from the subtotal.</p>
     */
    @NotNull(message = "Discount type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    /**
     * The discount value whose interpretation depends on the
     * {@link #discountType}.
     *
     * <p>For {@link DiscountType#PERCENTAGE}: a value between 0.01 and
     * 100.00 representing the percentage off the subtotal. For
     * {@link DiscountType#FIXED_AMOUNT}: a positive monetary amount
     * subtracted directly from the subtotal. Required field with a
     * precision of 10 digits total and 2 decimal places.</p>
     */
    @NotNull(message = "Discount value is required.")
    @DecimalMin(value = "0.01", message = "Discount value must be at least 0.01.")
    @DecimalMax(value = "99999999.99", message = "Discount value is too large.")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    /**
     * Minimum order subtotal required to apply this promo code.
     *
     * <p>When set, the customer's cart subtotal must meet or exceed
     * this value before the promo code can be applied. When {@code null},
     * no minimum order amount is enforced. Useful for running promotions
     * that require a minimum spend threshold (e.g. "Spend $50, save 10%").
     * Precision is 10 digits total with 2 decimal places.</p>
     */
    @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative.")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    /**
     * Maximum discount amount cap applicable only to percentage-based
     * discounts.
     *
     * <p>When set, a percentage discount will not exceed this monetary
     * amount regardless of the subtotal. For {@link DiscountType#FIXED_AMOUNT},
     * this field is ignored. When {@code null}, no cap is applied and the
     * full percentage discount is calculated against the subtotal. Useful
     * for limiting exposure on high-value orders (e.g. "10% off, max $20").
     * Precision is 10 digits total with 2 decimal places.</p>
     */
    @DecimalMin(value = "0.00", message = "Max discount amount cannot be negative.")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    /**
     * Cart composition constraint for this promo code, controlling which
     * cart configurations are eligible for the discount.
     *
     * <p>When set to {@link CouponType#SINGLE}, the code is only valid
     * when every cart item has quantity equal to 1 (single-unit purchase).
     * When set to {@link CouponType#BULK}, the code is only valid when
     * at least one cart item has quantity greater than 1 (multi-unit or
     * bulk purchase). When {@code null}, no composition constraint is
     * enforced and the code applies to any cart configuration.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", length = 10)
    private CouponType couponType;

    /**
     * Minimum unit quantity required for any single SKU in the cart
     * to qualify for this promo code.
     *
     * <p>When set, at least one cart item must have quantity greater
     * than or equal to this value. Useful for tiered promotions (e.g.
     * "buy 3, get 20% off") or BOGO codes ("buy 2, get 1 free").
     * When {@code null}, no minimum quantity check is performed on
     * individual cart items.</p>
     */
    @PositiveOrZero(message = "Minimum item quantity cannot be negative.")
    @Column(name = "min_item_quantity")
    private Integer minItemQuantity;

    /**
     * Maximum total number of times this code can be used across all
     * users combined.
     *
     * <p>When set, the code becomes invalid once the {@code usedCount}
     * reaches this limit. When {@code null}, usage is unlimited and
     * the code remains valid for as many redemptions as desired. The
     * counter is incremented atomically at checkout time to prevent
     * race conditions under concurrent usage.</p>
     */
    @PositiveOrZero(message = "Usage limit cannot be negative.")
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /**
     * Current number of times this promo code has been redeemed across
     * all users.
     *
     * <p>Incremented atomically at checkout time via the
     * {@link #incrementUsedCount()} method. Defaults to 0 for newly
     * created codes. Compared against {@link #usageLimit} to determine
     * whether the code has reached its global redemption cap.</p>
     */
    @PositiveOrZero(message = "Used count cannot be negative.")
    @Builder.Default
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    /**
     * Maximum number of times a single user can use this promo code.
     *
     * <p>When set, a user cannot apply this code more than the specified
     * number of times. Per-user usage is tracked through the
     * {@link PromoCodeUsage} join entity. When {@code null}, per-user
     * usage is unlimited and a user can apply the code on every order
     * as long as global limits are not exceeded.</p>
     */
    @PositiveOrZero(message = "User usage limit cannot be negative.")
    @Column(name = "user_usage_limit")
    private Integer userUsageLimit;

    /**
     * Timestamp when this promo code becomes valid and can be applied
     * at checkout.
     *
     * <p>Required field. The code is not valid for application before
     * this timestamp. Used in conjunction with {@link #validUntil} to
     * define the promotional window. Stored as an {@link Instant} for
     * timezone-independent validity checking.</p>
     */
    @NotNull(message = "Valid-from date is required.")
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    /**
     * Timestamp when this promo code expires and can no longer be
     * applied at checkout.
     *
     * <p>Required field. The code is not valid for application after
     * this timestamp. Used in conjunction with {@link #validFrom} to
     * define the promotional window. Stored as an {@link Instant} for
     * timezone-independent expiry checking.</p>
     */
    @NotNull(message = "Valid-until date is required.")
    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    /**
     * Whether this promo code is currently active and eligible for
     * application at checkout.
     *
     * <p>Inactive codes cannot be applied even if they fall within the
     * validity window defined by {@link #validFrom} and {@link #validUntil}.
     * Defaults to {@code true} for newly created codes. Administrators
     * can toggle this flag to pause or resume promotions without
     * deleting the code record.</p>
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Increments the global usage counter.
     */
    public void incrementUsedCount() {
        this.usedCount = (this.usedCount == null ? 0 : this.usedCount) + 1;
    }
}
