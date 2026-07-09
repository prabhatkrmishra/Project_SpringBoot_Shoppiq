package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.enums.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a promotional code (coupon) that can be applied during checkout.
 *
 * <p>A {@code PromoCode} defines a discount rule that customers can apply
 * to their order subtotal. It supports both percentage-based and fixed-amount
 * discounts, with optional minimum order requirements and usage limits.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores the code string and display description.</li>
 *     <li>Defines discount type and value.</li>
 *     <li>Enforces validity window, global usage limit, and per-user usage limit.</li>
 *     <li>Enforces minimum order amount.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} to inherit persistence identity,
 *     optimistic locking and auditing support.</li>
 *     <li>Code is stored uppercase and must be globally unique.</li>
 *     <li>{@code usedCount} is incremented atomically at checkout time.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * The promo code string entered by the customer.
     *
     * <p>Stored uppercase; uniqueness is enforced at the database level.</p>
     */
    @NotBlank(message = "Promo code is required.")
    @Size(max = 50, message = "Promo code cannot exceed 50 characters.")
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Human-readable description shown to the customer.
     */
    @Size(max = 255, message = "Description cannot exceed 255 characters.")
    @Column(length = 255)
    private String description;

    /**
     * How the discount value is interpreted.
     */
    @NotNull(message = "Discount type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    /**
     * The discount value.
     *
     * <p>For {@link DiscountType#PERCENTAGE}: a value between 0.01 and 100.00
     * representing the percentage off.</p>
     * <p>For {@link DiscountType#FIXED_AMOUNT}: a positive monetary amount
     * subtracted directly from the subtotal.</p>
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
     * <p>When {@code null}, no minimum is enforced.</p>
     */
    @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative.")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    /**
     * Maximum discount amount cap (applicable to percentage discounts only).
     *
     * <p>When set, a percentage discount will not exceed this amount.
     * For {@link DiscountType#FIXED_AMOUNT}, this field is ignored.</p>
     * <p>When {@code null}, no cap is applied.</p>
     */
    @DecimalMin(value = "0.00", message = "Max discount amount cannot be negative.")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    /**
     * Maximum total number of times this code can be used across all users.
     *
     * <p>When {@code null}, usage is unlimited.</p>
     */
    @PositiveOrZero(message = "Usage limit cannot be negative.")
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /**
     * Current number of times this code has been used.
     */
    @PositiveOrZero(message = "Used count cannot be negative.")
    @Builder.Default
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    /**
     * Maximum number of times a single user can use this code.
     *
     * <p>When {@code null}, per-user usage is unlimited.</p>
     */
    @PositiveOrZero(message = "User usage limit cannot be negative.")
    @Column(name = "user_usage_limit")
    private Integer userUsageLimit;

    /**
     * Timestamp when this promo code becomes valid.
     */
    @NotNull(message = "Valid-from date is required.")
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    /**
     * Timestamp when this promo code expires.
     */
    @NotNull(message = "Valid-until date is required.")
    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    /**
     * Whether this promo code is currently active.
     *
     * <p>Inactive codes cannot be applied even if within the validity window.</p>
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
