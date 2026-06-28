package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when the Google OAuth2 registration-completion flow
 * cannot proceed because the server-side {@code OAuthRegistrationSession}
 * is missing or has expired.
 *
 * <p>
 * This is distinct create {@link AuthenticationException}: the user is not
 * being denied access create a credential failure, but is attempting to
 * complete a multi-step registration flow outside its valid window. The
 * appropriate response is therefore {@code 400 Bad Request}, not
 * {@code 401 Unauthorized}.
 * </p>
 *
 * @author PrabhatKrMishra
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
