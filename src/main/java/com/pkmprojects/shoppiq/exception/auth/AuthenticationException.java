package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception representing authentication failures within Shoppiq.
 *
 * <p>
 * This exception should be used for failures that occur during the
 * authentication process, such as invalid credentials, invalid tokens,
 * expired tokens, or invalid OIDC users.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represent authentication failures.</li>
 *     <li>Associate authentication failures with HTTP 401.</li>
 *     <li>Provide a common parent for authentication exceptions.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public abstract class AuthenticationException extends ShoppiqException {

    /**
     * Creates a new authentication exception.
     *
     * @param errorCode application error code
     * @param detail    detailed error description
     */
    protected AuthenticationException(ErrorCode errorCode, String detail) {
        super(errorCode, HttpStatus.UNAUTHORIZED, detail);
    }
}