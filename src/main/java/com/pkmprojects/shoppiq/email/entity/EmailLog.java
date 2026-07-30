package com.pkmprojects.shoppiq.email.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity that records every email sent by the application for auditing and debugging.
 *
 * <p>Captures recipient, email type, delivery status, provider, and error details for each email attempt.
 * This entity provides a complete audit trail of all email communications, including successful
 * deliveries, failed attempts, and skipped emails due to user preferences. The log is used for
 * debugging email delivery issues and monitoring email service health.</p>
 *
 * <p>Each email log entry includes the recipient address, email type, subject, delivery status,
 * provider name, error message (if any), and timestamp. The entity extends AuditableEntity to
 * include creation and modification timestamps for comprehensive audit tracking.</p>
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
        /**
         * Email is queued but not yet sent.
         */
        PENDING,
        /**
         * Email was successfully delivered.
         */
        SENT,
        /**
         * Email delivery failed.
         */
        FAILED
    }
}
