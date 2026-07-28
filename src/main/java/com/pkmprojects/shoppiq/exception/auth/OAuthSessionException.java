package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Concrete leaf exception for
 * OAuth2 registration session expiration — an auth-domain error that
 * maps to {@code 400 Bad Request} rather than {@code 401 Unauthorized}.
 *
 * <p>Thrown when the Google OAuth2 registration-completion flow cannot
 * proceed because the server-side session is missing or has expired.
 * This is distinct from {@link AuthenticationException}: the user is not
 * being denied access from a credential failure, but is attempting to
 * complete a multi-step registration flow outside its valid window.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class OAuthSessionException extends InvalidOperationException {

    /**
     * Creates a new OAuthSessionException.
     *
     * @param detail detailed error description
     */
    public OAuthSessionException(String detail) {
        super(ErrorCode.OAUTH_SESSION_INVALID, detail);
    }
}
