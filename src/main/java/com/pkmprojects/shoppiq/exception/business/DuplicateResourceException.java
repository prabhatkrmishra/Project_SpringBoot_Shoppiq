package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception indicating that a resource already exists.
 *
 * <p>
 * Domain-specific duplicate exceptions should extend this class.
 * </p>
 *
 * <h2>Examples</h2>
 * <ul>
 *     <li>User already exists.</li>
 *     <li>Category already exists.</li>
 *     <li>SKU already exists.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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