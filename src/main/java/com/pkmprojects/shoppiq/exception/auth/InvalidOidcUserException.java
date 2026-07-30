package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when the authenticated OpenID Connect user cannot be processed.
 *
 * <p>This exception is thrown during the OAuth2 login flow when the OIDC
 * claims cannot be processed or the user's email is missing from the
 * token. It uses the {@link ErrorCode#INVALID_OIDC_USER} code and HTTP
 * 401 status. The detail message should explain what was wrong with the
 * OIDC token and what the user should do (e.g., "Unable to extract
 * email from Google token. Please try logging in again.").</p>
 *
 * <p>This exception is typically thrown by the OAuth2 success handler
 * or the authority mapper when the OIDC user attributes are incomplete
 * or invalid. The user should re-initiate the Google login flow.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#INVALID_OIDC_USER
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
