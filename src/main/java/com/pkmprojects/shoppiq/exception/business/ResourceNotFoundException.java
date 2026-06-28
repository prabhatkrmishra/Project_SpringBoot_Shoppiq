package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception indicating that a requested resource could not be found.
 *
 * <p>
 * Domain-specific "not found" exceptions should extend this class rather than
 * extending {@link ShoppiqException} directly.
 * </p>
 *
 * <h2>Typical Usage</h2>
 * <ul>
 *     <li>Item not found.</li>
 *     <li>Order not found.</li>
 *     <li>User not found.</li>
 *     <li>Cart not found.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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