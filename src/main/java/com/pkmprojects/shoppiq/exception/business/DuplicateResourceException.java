package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * <strong>Spring Boot Concept:</strong> Mid-level category exception in
 * the exception hierarchy — groups all "resource already exists" cases under
 * HTTP {@code 409 Conflict}.
 *
 * <p>Domain-specific duplicate exceptions (user, category, SKU, etc.)
 * should extend this class rather than extending
 * {@link com.pkmprojects.shoppiq.exception.base.ShoppiqException}
 * directly. The abstract parent pre-configures HTTP 409 so that all
 * subclasses automatically get the correct status without repeating it.</p>
 *
 * @author prabhatkrmishra
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
