package com.pkmprojects.shoppiq.verification;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores verification codes for email verification and password reset.
 *
 * <p>
 * Each code is single-use and expires after a configurable duration.
 * The code is linked to a user and an email type to support multiple
 * verification flows.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(name = "verification_codes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VerificationCode extends AuditableEntity {

    /**
     * Maximum allowed verification attempts before the code is invalidated.
     */
    public static final int MAX_ATTEMPTS = 3;

    /**
     * Code validity duration in minutes.
     */
    public static final int CODE_VALIDITY_MINUTES = 10;

    /**
     * The user this code belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_verification_codes_user")
    )
    private User user;

    /**
     * The verification code (6-digit numeric).
     */
    @Column(nullable = false, length = 10)
    private String code;

    /**
     * Type of verification (email verification, password reset).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailType emailType;

    /**
     * When the code expires.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether the code has been used.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    /**
     * Number of failed verification attempts.
     */
    @Builder.Default
    @Column(nullable = false)
    private int attempts = 0;

    /**
     * Checks if the code is still valid (not expired, not used, attempts not exceeded).
     *
     * @return true if valid
     */
    public boolean isValid() {
        return !used && attempts < MAX_ATTEMPTS && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Increments the attempt counter.
     */
    public void incrementAttempts() {
        this.attempts++;
    }

    /**
     * Marks the code as used.
     */
    public void markUsed() {
        this.used = true;
    }
}
