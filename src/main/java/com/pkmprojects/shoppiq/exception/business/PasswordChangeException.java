package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a password change request fails validation
 * (e.g. mismatched confirmation, or missing current password).
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public class PasswordChangeException extends InvalidOperationException {

    public PasswordChangeException(String detail) {
        super(ErrorCode.VALIDATION_FAILED, detail);
    }
}
