package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Concrete leaf exception in the
 * {@code business} branch for password change validation failures.
 *
 * <p>Thrown when a password change request fails validation (e.g. mismatched
 * confirmation, or missing current password). Shares the same error code
 * ({@link ErrorCode#CURRENT_PASSWORD_INCORRECT}) as
 * {@link CurrentPasswordIncorrectException} but conveys a different failure
 * scenario through its detail message — error codes are stable identifiers
 * for clients while detail messages provide human-readable context.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public class PasswordChangeException extends InvalidOperationException {

    /**
     * Creates a password change exception.
     *
     * @param detail the reason the password change failed
     */
    public PasswordChangeException(String detail) {
        super(ErrorCode.CURRENT_PASSWORD_INCORRECT, detail);
    }
}
