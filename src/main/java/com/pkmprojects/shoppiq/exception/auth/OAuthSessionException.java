package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when the OAuth2 registration session is missing, invalid, or has expired.
 *
 * <p>This exception is thrown during the OAuth2 registration completion
 * flow when the session cookie is absent or no longer valid. The session
 * stores the user's Google profile information and is used to complete
 * the local account creation. If the session has expired, the user must
 * re-initiate the Google login flow to create a new session.</p>
 *
 * <p>It uses the {@link ErrorCode#OAUTH_SESSION_INVALID} code and
 * HTTP 400 Bad Request status. The detail message should explain that
 * the session has expired and instruct the user to log in again.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#OAUTH_SESSION_INVALID
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
