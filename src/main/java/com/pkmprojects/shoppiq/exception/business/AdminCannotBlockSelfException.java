package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

public final class AdminCannotBlockSelfException extends ShoppiqException {

    public static AdminCannotBlockSelfException block() {
        return new AdminCannotBlockSelfException(
                ErrorCode.AUTH_BLOCK_SELF,
                "Administrators cannot disable their own account."
        );
    }

    public static AdminCannotBlockSelfException unblock() {
        return new AdminCannotBlockSelfException(
                ErrorCode.AUTH_UNBLOCK_SELF,
                "Administrators cannot enable their own account."
        );
    }

    private AdminCannotBlockSelfException(ErrorCode errorCode, String detail) {
        super(errorCode, HttpStatus.FORBIDDEN, detail);
    }
}
