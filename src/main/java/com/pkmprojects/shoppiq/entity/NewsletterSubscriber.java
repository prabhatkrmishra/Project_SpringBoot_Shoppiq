package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a newsletter subscriber who is not a registered Shoppiq user.
 *
 * <p>
 * Subscribers sign up via the homepage "Stay in the Loop" form and receive
 * promotional emails when admins use the "Send to all users" feature.
 * </p>
 *
 * @author PrabhatKrMishra
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
