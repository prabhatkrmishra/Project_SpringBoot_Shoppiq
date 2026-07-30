package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when JWT authentication fails due to an expired, malformed, or
 * invalid-signature token.
 *
 * <p>This exception is thrown by the JWT filter when it encounters a token
 * that cannot be processed. Common causes include: the token has expired
 * (use {@link ErrorCode#JWT_EXPIRED}), the token is malformed or cannot
 * be parsed (use {@link ErrorCode#INVALID_JWT}), or the token signature
 * does not match the signing key. The detail message should explain the
 * specific failure reason.</p>
 *
 * <p>When this exception is thrown, the JWT filter rejects the request
 * before it reaches the controller layer. The client should obtain a new
 * token through the login or refresh endpoint. The global exception
 * handler maps this to a 401 Problem Detail response.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#INVALID_JWT
 * @see ErrorCode#JWT_EXPIRED
 * @since 1.0.0
 */
public class JwtAuthenticationException extends AuthenticationException {

    /**
     * Creates a JWT authentication exception.
     *
     * @param errorCode authentication error code
     * @param detail    detailed error description
     */
    public JwtAuthenticationException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

}
