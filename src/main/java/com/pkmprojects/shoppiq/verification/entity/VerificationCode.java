package com.pkmprojects.shoppiq.verification.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Clock;
import java.time.Instant;

/**
 * JPA entity storing verification codes for email verification and password reset.
 *
 * <p>Supports single-use, time-limited codes with attempt tracking for security flows.
 * Each code is a 6-digit numeric string that expires after a configurable duration (default:
 * 10 minutes). The entity tracks usage status and failed attempt counts to prevent brute-force
 * attacks. Codes are automatically invalidated after successful validation or when the maximum
 * number of failed attempts is exceeded.</p>
 *
 * <p>This entity is used by the verification service to manage the lifecycle of verification
 * codes. When a new code is generated, any existing unused codes for the same user and email
 * type are automatically invalidated to prevent code reuse attacks.</p>
 *
 * @author prabhatkrmishra
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
    private Instant expiresAt;

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
     * Checks if the code is still valid using the system clock.
     *
     * @return true if valid
     */
    public boolean isValid() {
        return isValid(Clock.systemDefaultZone());
    }

    /**
     * Checks if the code is still valid (not expired, not used, attempts not exceeded).
     *
     * @param clock clock for deterministic time
     * @return true if valid
     */
    public boolean isValid(Clock clock) {
        return !used && attempts < MAX_ATTEMPTS && Instant.now(clock).isBefore(expiresAt);
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
