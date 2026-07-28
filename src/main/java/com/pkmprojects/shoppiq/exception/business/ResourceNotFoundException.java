package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * <strong>Spring Boot Concept:</strong> Mid-level category exception that
 * groups all "not found" cases under HTTP {@code 404 Not Found}.
 *
 * <p>Domain-specific "not found" exceptions (item, order, user, cart, etc.)
 * should extend this class. Together with
 * {@link DuplicateResourceException} (409), {@link InvalidOperationException}
 * (400), and {@link UnauthorizedOperationException} (403), this forms a
 * category hierarchy where each abstract parent maps to exactly one HTTP
 * status — the {@code @ControllerAdvice} handler derives the status from
 * the exception itself without if/else chains.</p>
 *
 * @author prabhatkrmishra
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
