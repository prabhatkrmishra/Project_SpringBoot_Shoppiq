package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all "resource already exists" cases under HTTP 409 Conflict.
 *
 * <p>This abstract class serves as the parent for all entity-specific
 * duplicate exceptions (e.g., DuplicateUserException, DuplicateItemException).
 * It hardcodes the HTTP status to {@link HttpStatus#CONFLICT} so that
 * subclasses only need to provide an {@link ErrorCode} and a detail
 * message. The global exception handler maps this to a 409 Problem
 * Detail response.</p>
 *
 * <p>Service methods should throw concrete subclasses of this exception
 * when an insert or update operation violates a unique constraint. The
 * detail message should explain which field is duplicated and suggest
 * how the client can resolve the conflict (e.g., "A user with this email
 * already exists. Please use a different email or proceed to login.").</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.exception.base.ShoppiqException
 * @since 1.0.0
 */
public abstract class DuplicateResourceException extends ShoppiqException {

    /**
     * Creates a duplicate resource exception.
     *
     * @param errorCode machine-readable error code
     * @param detail    detailed error message
     */
    protected DuplicateResourceException(ErrorCode errorCode, String detail) {
        super(errorCode, HttpStatus.CONFLICT, detail);
    }

}
