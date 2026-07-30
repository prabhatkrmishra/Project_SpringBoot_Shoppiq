package com.pkmprojects.shoppiq.entity.newsletter;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

/**
 * Represents a newsletter subscriber who is not a registered Shoppiq user.
 *
 * <p>Subscribers sign up via the homepage "Stay in the Loop" form and
 * receive promotional emails when admins use the "Send to all users"
 * feature. Uses a token-based identity for secure unsubscription links
 * without requiring authentication, enabling guest subscribers to manage
 * their preferences independently.</p>
 *
 * <p>Inactive subscribers (those who have unsubscribed) are excluded from
 * mailings but their records are preserved for analytics and compliance
 * purposes. The {@code active} flag acts as a soft-delete mechanism,
 * allowing re-subscription by toggling the flag back to {@code true}.
 * Each subscriber record includes timestamps for both subscription and
 * unsubscription events.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Entity
@Table(name = "newsletter_subscribers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class NewsletterSubscriber extends AuditableEntity {

    /**
     * Subscriber email address used for promotional email delivery.
     *
     * <p>Globally unique; prevents duplicate subscriptions. Validated
     * at the application layer to ensure proper email format. This
     * email is used as the delivery target for newsletter campaigns
     * and promotional announcements.</p>
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Secure token (UUID format) used in unsubscription links to
     * authenticate guest subscribers without requiring login.
     *
     * <p>Globally unique; generated at subscription time using
     * {@code UUID.randomUUID()}. This token enables token-based
     * identity for guests who are not authenticated, allowing them
     * to unsubscribe via a URL containing this token. Maximum length
     * of 36 characters to accommodate UUID string representation.</p>
     */
    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token;

    /**
     * Whether this subscriber is currently active and eligible to
     * receive promotional emails.
     *
     * <p>Soft-delete flag: inactive subscribers are excluded from
     * mailings but their records are preserved for analytics and
     * compliance. Defaults to {@code true} for new subscriptions.
     * Set to {@code false} when the subscriber unsubscribes via the
     * token-based link. Can be toggled back to {@code true} for
     * re-subscription scenarios.</p>
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Timestamp when the subscription was created and the subscriber
     * first opted in to receive promotional emails.
     *
     * <p>Set to the current UTC time at the moment of subscription.
     * Used for subscription analytics, cohort analysis, and compliance
     * auditing. Stored as an {@link Instant} for timezone independence.</p>
     */
    @Column(name = "subscribed_at", nullable = false)
    private Instant subscribedAt;

    /**
     * Timestamp when the subscription was cancelled via the
     * unsubscription link.
     *
     * <p>Null if the subscriber is still active. When populated,
     * indicates the exact moment the subscriber opted out of future
     * mailings. Used for unsubscribe analytics, compliance auditing,
     * and calculating subscription duration for reporting purposes.</p>
     */
    @Column(name = "unsubscribed_at")
    private Instant unsubscribedAt;
}
