package com.pkmprojects.shoppiq.verification;

import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.User;

/**
 * Business contract for managing verification codes.
 *
 * @author PrabhatKrMishra
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
     * @throws com.pkmprojects.shoppiq.exception.VerificationCodeException if invalid
     */
    boolean validateCode(Long userId, String code, EmailType emailType);
}
