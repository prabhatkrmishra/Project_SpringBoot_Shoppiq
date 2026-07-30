package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all authentication failures under HTTP 401 Unauthorized.
 *
 * <p>This abstract class serves as the parent for all authentication-related
 * exceptions (e.g., {@link InvalidCredentialException},
 * {@link JwtAuthenticationException}, {@link InvalidOidcUserException}).
 * It hardcodes the HTTP status to {@link HttpStatus#UNAUTHORIZED} so that
 * subclasses only need to provide an {@link ErrorCode} and a detail
 * message. The global exception handler maps this to a 401 Problem
 * Detail response.</p>
 *
 * <p>This exception is distinct from
 * {@link com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException},
 * which uses HTTP 403 Forbidden. A 401 response means the user is not
 * identified (missing or invalid credentials), while a 403 response means
 * the identified user lacks permission to perform the action.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException
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
