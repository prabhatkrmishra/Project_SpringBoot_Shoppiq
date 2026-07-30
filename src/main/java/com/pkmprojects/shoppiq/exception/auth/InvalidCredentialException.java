package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when authentication fails due to invalid credentials.
 *
 * <p>This exception is thrown during the email/password login flow when
 * the submitted credentials do not match any record in the database.
 * It uses the {@link ErrorCode#INVALID_CREDENTIALS} code. The detail
 * message should be generic enough to prevent username enumeration
 * attacks (e.g., "Invalid email or password.") rather than specifying
 * which field was incorrect.</p>
 *
 * <p>This exception extends {@link InvalidOperationException} rather
 * than {@link AuthenticationException} because it is typically thrown
 * by the authentication service during credential validation, not by
 * the JWT filter. The HTTP 401 status is inherited from the
 * {@link ErrorCode} mapping.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#INVALID_CREDENTIALS
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
