package com.pkmprojects.shoppiq.verification.service;

import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.general.verification.VerificationCodeException;

/**
 * <strong>Spring Boot Concept:</strong> Business contract for managing verification codes used in email
 * verification and password reset flows.
 *
 * <p><b>How it fits:</b> Supports user authentication and account
 * recovery — authenticated users can access AI chat features.
 * Verification codes are generated with {@link java.security.SecureRandom}
 * and have single-use + expiry semantics.</p>
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
