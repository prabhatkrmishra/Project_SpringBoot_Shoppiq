package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception indicating that the current user
 * is not allowed to perform the requested operation.
 *
 * @author PrabhatKrMishra
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