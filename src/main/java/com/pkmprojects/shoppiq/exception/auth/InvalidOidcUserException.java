package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Concrete leaf exception in the
 * {@code auth} branch for OpenID Connect (OIDC) authentication failures.
 *
 * <p>Thrown when the authenticated OIDC user cannot be processed. This may
 * occur when mandatory claims are missing or the OIDC provider returns
 * an unsupported user profile.</p>
 *
 * @author prabhatkrmishra
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
