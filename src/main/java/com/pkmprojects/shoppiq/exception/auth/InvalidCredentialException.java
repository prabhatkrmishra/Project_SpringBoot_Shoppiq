package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Concrete leaf exception for
 * credential validation failures. Note the cross-branch inheritance:
 * this extends {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (400 BAD_REQUEST) rather than {@link AuthenticationException} (401).
 *
 * <p>Thrown when authentication fails due to invalid credentials.
 * Maps to {@link ErrorCode#INVALID_CREDENTIALS} and results in a
 * {@code 401 Unauthorized} response with a generic error message to
 * avoid leaking whether the username or password was incorrect.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public class InvalidCredentialException extends InvalidOperationException {

    /**
     * Creates a new InvalidCredentialException.
     *
     * @param detail detailed error description
     */
    public InvalidCredentialException(String detail) {
        super(ErrorCode.INVALID_CREDENTIALS, detail);
    }
}
