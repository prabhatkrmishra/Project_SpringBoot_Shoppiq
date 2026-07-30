package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception for business rule violations under HTTP 400 Bad Request.
 *
 * <p>This abstract class serves as the parent for all invalid-operation
 * exceptions (e.g., {@link InvalidRequestException},
 * {@link PasswordChangeException}, {@link CurrentPasswordIncorrectException}).
 * It hardcodes the HTTP status to {@link HttpStatus#BAD_REQUEST} so that
 * subclasses only need to provide an {@link ErrorCode} and a detail
 * message. The global exception handler maps this to a 400 Problem
 * Detail response.</p>
 *
 * <p>Service methods should throw concrete subclasses of this exception
 * when a business rule is violated. The detail message should clearly
 * explain what operation was attempted, why it failed, and what the
 * client should do to resolve the issue.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.exception.base.ShoppiqException
 * @since 1.0.0
 */
public abstract class InvalidOperationException extends ShoppiqException {

    /**
     * Creates an invalid operation exception.
     *
     * @param errorCode machine-readable error code
     * @param detail    detailed message
     */
    protected InvalidOperationException(ErrorCode errorCode, String detail) {
        super(errorCode, HttpStatus.BAD_REQUEST, detail);
    }

}
