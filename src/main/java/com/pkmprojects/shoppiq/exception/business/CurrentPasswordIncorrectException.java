package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when the current password supplied during a password change does not match.
 *
 * <p>This exception is thrown by the password change service when the
 * user-provided current password does not match the stored hash. It uses
 * the {@link ErrorCode#CURRENT_PASSWORD_INCORRECT} code and HTTP 400
 * Bad Request status. This is a security measure to prevent unauthorized
 * password changes by ensuring the user knows their current password.</p>
 *
 * <p>The exception uses a fixed detail message ("Current password is
 * incorrect.") to avoid leaking information about whether the account
 * exists or which field was incorrect.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#CURRENT_PASSWORD_INCORRECT
 * @since 1.0.0
 */
public class CurrentPasswordIncorrectException extends InvalidOperationException {

    /**
     * Creates a current password incorrect exception.
     */
    public CurrentPasswordIncorrectException() {
        super(ErrorCode.CURRENT_PASSWORD_INCORRECT, "Current password is incorrect.");
    }
}
