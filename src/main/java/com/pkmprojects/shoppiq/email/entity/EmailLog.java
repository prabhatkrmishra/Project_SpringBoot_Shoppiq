package com.pkmprojects.shoppiq.email.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * <strong>Spring Boot Concept:</strong> JPA entity that records every email
 * sent by the application for auditing and debugging purposes. This is an
 * example of the <strong>Audit Log</strong> pattern in a layered Spring Boot
 * application.
 *
 * <p>
 * Each row captures the recipient, email type, delivery status, and
 * the provider used to send the email. Failed sends include the error message.
 * </p>
 *
 * <p><strong>Educational value:</strong> This entity demonstrates:
 * <ul>
 *   <li><strong>JPA entity mapping</strong> — {@code @Entity}, {@code @Table},
 *       {@code @Column}, {@code @Enumerated}, and {@code @ManyToOne}
 *       annotations for relational mapping.</li>
 *   <li><strong>AuditableEntity inheritance</strong> — extends a base entity
 *       that provides created-at, updated-at, and other audit fields
 *       (common Spring Data JPA pattern using {@code @EntityListeners} or
 *       {@code AuditingEntityListener}).</li>
 *   <li><strong>Lazy fetching</strong> — the {@code User} relationship uses
 *       {@code FetchType.LAZY} for performance, with a foreign key constraint
 *       named explicitly via {@code @ForeignKey}.</li>
 *   <li><strong>Nested enum</strong> — {@code EmailStatus} is defined as a
 *       nested enum for cohesive domain modeling (PENDING → SENT / FAILED).</li>
 *   <li><strong>Lombok boilerplate reduction</strong> — uses {@code @Getter},
 *       {@code @Setter}, {@code @Builder}, {@code @NoArgsConstructor},
 *       {@code @AllArgsConstructor}, and
 *       {@code @EqualsAndHashCode(callSuper = true)}.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Entity
@Table(name = "email_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailLog extends AuditableEntity {

    /**
     * User who triggered the email (nullable for system emails).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_email_logs_user")
    )
    private User user;

    /**
     * Type of email sent.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailType emailType;

    /**
     * Recipient email address.
     */
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    /**
     * Email subject line.
     */
    @Column(nullable = false, length = 255)
    private String subject;

    /**
     * Delivery status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailStatus status;

    /**
     * Provider used to send the email.
     */
    @Column(length = 50)
    private String provider;

    /**
     * Error message if sending failed.
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Timestamp when the email was sent (or attempted).
     */
    @Column(name = "sent_at")
    private java.time.Instant sentAt;

    /**
     * Email delivery status.
     */
    public enum EmailStatus {
        PENDING,
        SENT,
        FAILED
    }
}
