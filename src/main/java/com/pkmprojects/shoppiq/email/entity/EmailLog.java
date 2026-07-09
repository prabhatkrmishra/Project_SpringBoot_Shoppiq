package com.pkmprojects.shoppiq.email.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.email.EmailType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Records every email sent by the application for auditing and debugging.
 *
 * <p>
 * Each row captures the recipient, email type, delivery status, and
 * the provider used to send the email. Failed sends include the error message.
 * </p>
 *
 * @author PrabhatKrMishra
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
    private com.pkmprojects.shoppiq.entity.User user;

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
    private java.time.LocalDateTime sentAt;

    /**
     * Email delivery status.
     */
    public enum EmailStatus {
        PENDING,
        SENT,
        FAILED
    }
}
