package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Records a single usage of a {@link PromoCode} by a {@link User} on an {@link Order}.
 *
 * <p>This entity enables per-user usage tracking and prevents the same order
 * from applying multiple promo codes.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Links a promo code to the user who redeemed it.</li>
 *     <li>Links to the order where the code was applied.</li>
 *     <li>Captures the timestamp of usage.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} to inherit persistence identity,
 *     optimistic locking and auditing support.</li>
 *     <li>A unique constraint on {@code order_id} ensures at most one promo
 *     code per order.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(
        name = "promo_code_usage",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_promo_usage_order",
                        columnNames = "order_id"
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
     * The promo code that was used.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "promo_code_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_promo_usage_promo_code")
    )
    private PromoCode promoCode;

    /**
     * The user who redeemed the code.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_promo_usage_user")
    )
    private User user;

    /**
     * The order on which the code was applied.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_promo_usage_order")
    )
    private Order order;

    /**
     * Timestamp when the code was used.
     */
    @Column(name = "used_at", nullable = false)
    private Instant usedAt;
}
