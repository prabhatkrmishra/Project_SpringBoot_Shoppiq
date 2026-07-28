package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Concrete leaf exception in the
 * {@code business} branch — thrown when the current password supplied
 * during a password change does not match the stored password.
 *
 * <p>A leaf-level exception that extends {@link InvalidOperationException}
 * (HTTP 400) and carries the
 * {@link ErrorCode#CURRENT_PASSWORD_INCORRECT} error code.</p>
 *
 * @author prabhatkrmishra
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
