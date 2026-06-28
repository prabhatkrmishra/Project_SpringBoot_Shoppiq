package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when JWT authentication fails.
 *
 * <p>
 * Typical causes include:
 * </p>
 *
 * <ul>
 *     <li>Expired JWT.</li>
 *     <li>Malformed JWT.</li>
 *     <li>Invalid signature.</li>
 *     <li>Unsupported JWT.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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