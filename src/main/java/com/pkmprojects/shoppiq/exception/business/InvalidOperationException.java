package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception indicating that an operation
 * cannot be performed in the current state.
 *
 * @author PrabhatKrMishra
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