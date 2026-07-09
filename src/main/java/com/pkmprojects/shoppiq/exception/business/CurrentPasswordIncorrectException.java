package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when the current password supplied during a password change
 * does not match the stored password.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public class CurrentPasswordIncorrectException extends InvalidOperationException {

    public CurrentPasswordIncorrectException() {
        super(ErrorCode.CURRENT_PASSWORD_INCORRECT, "Current password is incorrect.");
    }
}
