package com.pkmprojects.shoppiq.entity.promo;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Records a single usage of a {@link PromoCode} by a {@link User} on
 * an {@link Order}.
 *
 * <p>Enables per-user usage tracking and prevents the same order from
 * applying multiple promo codes. Links the promo code, the user who
 * redeemed it, and the order where it was applied. A unique constraint
 * on {@code (promo_code_id, user_id)} enforces the per-user limit at
 * the database level, while a unique constraint on {@code order_id}
 * prevents multiple promo codes on a single order.</p>
 *
 * <p>Immutable once created for audit integrity. This entity is not
 * cascade-deleted when the promo code is deactivated or the order is
 * cancelled, preserving a complete redemption history for analytics
 * and compliance purposes.</p>
 *
 * @author prabhatkrmishra
 * @see PromoCode
 * @see User
 * @see com.pkmprojects.shoppiq.entity.order.Order
 * @since 1.0.0
 */
@Entity
@Table(
        name = "promo_code_usage",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_promo_usage_order",
                        columnNames = "order_id"
                ),
                @UniqueConstraint(
                        name = "uk_promo_usage_user",
                        columnNames = {"promo_code_id", "user_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PromoCodeUsage extends AuditableEntity {

    /**
     * The promo code that was redeemed in this transaction.
     *
     * <p>Required relationship. Each usage record is associated with
     * exactly one promo code. The promo code reference is lazily loaded
     * to avoid unnecessary joins when querying usage history. This FK
     * is used to enforce per-user limits and to compute remaining
     * global usage quotas.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "promo_code_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_promo_usage_promo_code")
    )
    private PromoCode promoCode;

    /**
     * The user who redeemed the promo code.
     *
     * <p>Required relationship. Each usage record is associated with
     * exactly one user. The user reference is lazily loaded to avoid
     * unnecessary joins when querying promo usage. This FK is used
     * to enforce per-user usage limits defined by the promo code's
     * {@code userUsageLimit} field.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_promo_usage_user")
    )
    private User user;

    /**
     * The order on which the promo code was applied.
     *
     * <p>Required relationship. Each usage record is associated with
     * exactly one order. A unique constraint on {@code order_id}
     * ensures that only one promo code can be applied per order. The
     * order reference is lazily loaded to avoid unnecessary joins
     * when querying usage patterns.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_promo_usage_order")
    )
    private Order order;

    /**
     * Timestamp when the promo code was redeemed by the user.
     *
     * <p>Set to the current UTC time at the moment of redemption during
     * checkout. Used for usage analytics, time-based reporting, and
     * compliance auditing. Stored as an {@link Instant} for timezone
     * independence.</p>
     */
    @Column(name = "used_at", nullable = false)
    private Instant usedAt;
}
