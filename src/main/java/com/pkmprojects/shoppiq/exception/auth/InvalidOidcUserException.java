package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when the authenticated OIDC user cannot be processed.
 *
 * <p>
 * This may occur when mandatory claims are missing or when the OIDC
 * provider returns an unsupported user profile.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public class InvalidOidcUserException extends AuthenticationException {

    /**
     * Creates a new InvalidOidcUserException.
     *
     * @param detail detailed error description
     */
    public InvalidOidcUserException(String detail) {
        super(ErrorCode.INVALID_OIDC_USER, detail);
    }
}