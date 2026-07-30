package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a password change request fails validation.
 *
 * <p>This exception is used when the password change process encounters
 * a validation error that is not covered by more specific exceptions.
 * It uses the {@link ErrorCode#CURRENT_PASSWORD_INCORRECT} code and
 * HTTP 400 Bad Request status. The detail message should explain what
 * validation rule was violated.</p>
 *
 * <p>For the specific case where the current password does not match,
 * prefer using {@link CurrentPasswordIncorrectException} instead, as
 * it provides a more specific error message without requiring a
 * parameter.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#CURRENT_PASSWORD_INCORRECT
 * @see CurrentPasswordIncorrectException
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
