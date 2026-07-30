package com.pkmprojects.shoppiq.verification.service;

import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.general.verification.VerificationCodeException;

/**
 * Business contract for managing verification codes in email verification and password reset flows.
 *
 * <p>Provides code generation with SecureRandom and validation with single-use and expiry semantics.
 * The service ensures that verification codes are cryptographically secure, time-limited, and
 * protected against brute-force attacks through attempt tracking. Each code is single-use and
 * automatically invalidated after successful validation or when the maximum number of failed
 * attempts is exceeded.</p>
 *
 * <p>This service is used by the authentication and registration flows to verify user identity
 * through email-based verification codes. The generated codes are 6-digit numeric strings
 * that expire after a configurable duration (default: 10 minutes).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface VerificationCodeService {

    /**
     * Generates a new verification code for the user and email type.
     *
     * <p>
     * Any existing unused codes for this user and type are invalidated.
     * </p>
     *
     * @param user      the user
     * @param emailType the type of verification
     * @return the generated code
     */
    String generateCode(User user, EmailType emailType);

    /**
     * Validates a verification code.
     *
     * @param userId    the user ID
     * @param code      the code to validate
     * @param emailType the email type
     * @return true if valid
     * @throws VerificationCodeException if invalid
     */
    boolean validateCode(Long userId, String code, EmailType emailType);
}
