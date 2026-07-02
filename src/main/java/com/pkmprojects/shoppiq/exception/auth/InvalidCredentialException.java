package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

public class InvalidCredentialException extends InvalidOperationException {

    /**
     * Creates a new InvalidCredentialException.
     *
     * @param detail detailed error description
     */
    public InvalidCredentialException(String detail) {
        super(ErrorCode.INVALID_CREDENTIALS, detail);
    }
}
