package com.pkmprojects.shoppiq.entity.notification;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Stores a user's email notification preferences controlling which
 * categories of email they receive.
 *
 * <p>Each user owns at most one {@code NotificationPreference} row,
 * enforced by a unique constraint on the {@code user_id} column. Four
 * boolean flags control which categories of email the user has opted
 * into: order updates, account security alerts, promotional offers,
 * and review/engagement notifications. All flags default to
 * {@code true} so new users receive every category unless they
 * explicitly disable one or more.</p>
 *
 * <p>The service layer checks these preferences before sending any
 * email notification, ensuring that user consent is respected across
 * all communication channels. Preferences can be updated at any time
 * through the user's account settings page.</p>
 *
 * @author prabhatkrmishra
 * @see User
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
     * The user these notification preferences belong to.
     *
     * <p>One-to-one relationship: each user has at most one preference
     * record. The {@code user_id} column carries a unique constraint
     * to enforce this invariant at the database level. The user
     * reference is lazily loaded to avoid unnecessary joins when
     * checking notification preferences.</p>
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
     * Whether the user has opted in to receive order lifecycle email
     * notifications, including order placed, shipped, delivered,
     * cancelled, and refund confirmations.
     *
     * <p>Defaults to {@code true} for new users. When set to
     * {@code false}, the user will not receive any order-related
     * email updates, which may impact their ability to track order
     * status without manually checking the platform.</p>
     */
    @Builder.Default
    @Column(name = "order_updates", nullable = false)
    private boolean orderUpdates = true;

    /**
     * Whether the user has opted in to receive account and security
     * email notifications, including password change confirmations,
     * new login alerts, and account verification emails.
     *
     * <p>Defaults to {@code true} for new users. When set to
     * {@code false}, the user will not receive security-related
     * email alerts, which may reduce their awareness of unauthorized
     * account access attempts.</p>
     */
    @Builder.Default
    @Column(name = "account_security", nullable = false)
    private boolean accountSecurity = true;

    /**
     * Whether the user has opted in to receive promotional email
     * notifications, including marketing campaigns, discount offers,
     * and sale event alerts.
     *
     * <p>Defaults to {@code true} for new users. When set to
     * {@code false}, the user will not receive any marketing or
     * promotional emails. This preference is checked before sending
     * any bulk promotional campaigns through the admin newsletter
     * system.</p>
     */
    @Builder.Default
    @Column(name = "promotions", nullable = false)
    private boolean promotions = true;

    /**
     * Whether the user has opted in to receive review and engagement
     * email notifications, including review reminders, seller replies
     * to reviews, and price drop alerts for wishlisted items.
     *
     * <p>Defaults to {@code true} for new users. When set to
     * {@code false}, the user will not receive follow-up emails
     * related to their review activity or product price changes.
     * This helps users reduce email clutter while still using the
     * platform's core features.</p>
     */
    @Builder.Default
    @Column(name = "reviews_engagement", nullable = false)
    private boolean reviewsEngagement = true;
}
