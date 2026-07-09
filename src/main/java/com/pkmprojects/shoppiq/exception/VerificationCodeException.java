package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a verification code is invalid or expired.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public class VerificationCodeException extends ShoppiqException {

    /**
     * Creates a new VerificationCodeException.
     *
     * @param errorCode the specific verification error code
     * @param detail    error description
     */
    public VerificationCodeException(ErrorCode errorCode, String detail) {
        super(errorCode, HttpStatus.BAD_REQUEST, detail);
    }
}
