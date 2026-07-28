package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * <strong>Spring Boot Concept:</strong> Mid-level category exception in the
 * {@code auth} package — groups all authentication failures under HTTP
 * {@code 401 Unauthorized}.
 *
 * <p>Thrown when authentication fails (invalid credentials, invalid or
 * expired JWT, invalid OIDC user). Together with
 * {@link com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException}
 * (403), these two form the security-related branches of the exception
 * hierarchy: 401 indicates the client is not authenticated, while 403
 * indicates the client is authenticated but lacks required permissions.</p>
 *
 * @author prabhatkrmishra
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
