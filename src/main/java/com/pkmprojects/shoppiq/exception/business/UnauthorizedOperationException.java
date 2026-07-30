package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception for "forbidden" cases under HTTP 403 Forbidden.
 *
 * <p>This abstract class serves as the parent for all authorization
 * failure exceptions (e.g., access-denied errors for resources that
 * belong to another user). It hardcodes the HTTP status to
 * {@link HttpStatus#FORBIDDEN} so that subclasses only need to provide
 * an {@link ErrorCode} and a detail message. The global exception
 * handler maps this to a 403 Problem Detail response.</p>
 *
 * <p>This exception is distinct from authentication failures (HTTP 401).
 * A 403 response means the user is authenticated but lacks permission
 * to perform the requested action. Service methods should throw this
 * when ownership or role checks fail.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.exception.base.ShoppiqException
 * @see com.pkmprojects.shoppiq.exception.auth.AuthenticationException
 * @since 1.0.0
 */
public abstract class UnauthorizedOperationException extends ShoppiqException {

    /**
     * Creates an unauthorized operation exception.
     *
     * @param errorCode machine-readable error code
     * @param detail    detailed error message
     */
    protected UnauthorizedOperationException(ErrorCode errorCode, String detail) {
        super(errorCode, HttpStatus.FORBIDDEN, detail);
    }

}
