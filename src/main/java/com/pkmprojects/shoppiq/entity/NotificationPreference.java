package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Stores a user's email notification preferences.
 *
 * <p>
 * Each user owns at most one {@code NotificationPreference} row. The four
 * boolean flags control which categories of email the user has opted into.
 * All flags default to {@code true} so new users receive every category
 * unless they explicitly disable it.
 * </p>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} for id, version and timestamps.</li>
 *     <li>Relationship is owned by this entity via a {@code user_id} FK.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationPreference extends AuditableEntity {

    /**
     * The user these preferences belong to.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            unique = true,
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_preferences_user")
    )
    private User user;

    /**
     * Order lifecycle emails (placed, shipped, delivered, cancelled, refunds).
     */
    @Builder.Default
    @Column(name = "order_updates", nullable = false)
    private boolean orderUpdates = true;

    /**
     * Account &amp; security emails (password changed, new login alerts).
     */
    @Builder.Default
    @Column(name = "account_security", nullable = false)
    private boolean accountSecurity = true;

    /**
     * Promotional emails (marketing, discounts, sale alerts).
     */
    @Builder.Default
    @Column(name = "promotions", nullable = false)
    private boolean promotions = true;

    /**
     * Review &amp; engagement emails (review reminders, replies, price drops).
     */
    @Builder.Default
    @Column(name = "reviews_engagement", nullable = false)
    private boolean reviewsEngagement = true;
}
