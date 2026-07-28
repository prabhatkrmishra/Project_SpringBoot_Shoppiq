package com.pkmprojects.shoppiq.entity.newsletter;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Represents a newsletter subscriber who is not a registered Shoppiq user.
 *
 * <p>
 * Subscribers sign up via the homepage "Stay in the Loop" form and receive
 * promotional emails when admins use the "Send to all users" feature.
 * </p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Unique email constraint</strong> — {@code unique = true} on
 *         the email column prevents duplicate subscriptions at the database
 *         level.</li>
 *     <li><strong>Token-based identity</strong> — The {@code token} field
 *         (UUID, length 36) enables secure unsubscription links without
 *         requiring authentication. This is a common pattern for guest users.</li>
 *     <li><strong>Audit trail via timestamps</strong> — {@code subscribedAt}
 *         and {@code unsubscribedAt} track the lifecycle explicitly, separate
 *         from the inherited {@code createdAt}/{@code updatedAt} audit fields.</li>
 *     <li><strong>{@code active} flag</strong> — Soft-delete pattern: instead
 *         of deleting rows, the subscriber is marked inactive. Preserves the
 *         record for analytics while excluding them from mailings.</li>
 *     <li><strong>Extends {@link AuditableEntity}</strong> — Inherits ID,
 *         version, and automatic audit timestamps.</li>
 *     <li><strong>{@code @EqualsAndHashCode(of = "id", callSuper = false)}</strong>
 *         — Uses only the local {@code id} for equality, explicitly excluding
 *         superclass fields.</li>
 * </ul>
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

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "subscribed_at", nullable = false)
    private Instant subscribedAt;

    @Column(name = "unsubscribed_at")
    private Instant unsubscribedAt;
}
