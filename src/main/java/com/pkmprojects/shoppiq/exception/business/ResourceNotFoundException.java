package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all "not found" cases under HTTP 404 Not Found.
 *
 * <p>This abstract class serves as the parent for all entity-specific
 * not-found exceptions (e.g., UserNotFoundException, ItemNotFoundException).
 * It hardcodes the HTTP status to {@link HttpStatus#NOT_FOUND} so that
 * subclasses only need to provide an {@link ErrorCode} and a detail
 * message. The global exception handler maps this to a 404 Problem
 * Detail response.</p>
 *
 * <p>Service methods should throw concrete subclasses of this exception
 * when an entity lookup fails. The detail message should explain which
 * entity was not found and how the client can resolve the issue (e.g.,
 * "User not found with ID: 42. Verify the user ID and try again.").</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.exception.base.ShoppiqException
 * @since 1.0.0
 */
public abstract class ResourceNotFoundException extends ShoppiqException {

    /**
     * Creates a resource not found exception.
     *
     * @param errorCode machine-readable error code
     * @param detail    detailed description
     */
    protected ResourceNotFoundException(ErrorCode errorCode, String detail) {
        super(errorCode, HttpStatus.NOT_FOUND, detail);
    }

}
