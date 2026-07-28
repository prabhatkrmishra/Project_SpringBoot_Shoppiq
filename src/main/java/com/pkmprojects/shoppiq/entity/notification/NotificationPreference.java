package com.pkmprojects.shoppiq.entity.notification;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * <strong>Spring Boot Concept:</strong> Stores a user's email notification preferences.
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
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>{@code @OneToOne} owned by preference</strong> — The FK is
 *         on this table (not on the {@code users} table), which is the
 *         standard pattern when the relationship is optional or when the
 *         child entity is added later.</li>
 *     <li><strong>Boolean flag pattern</strong> — Four boolean columns
 *         categorize notification types. This is simpler than a separate
 *         "notification types" join table and works well when the number of
 *         categories is small and stable.</li>
 *     <li><strong>Opt-out defaults</strong> — {@code @Builder.Default true}
 *         on all flags means new users are automatically subscribed to every
 *         category, implementing an "opt-out" model rather than "opt-in".</li>
 *     <li><strong>{@code @ForeignKey(name = "fk_notification_preferences_user")}</strong>
 *         — Named FK constraint for readable schema and easier debugging of
 *         constraint violations.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
