package com.pkmprojects.shoppiq.exception.admin;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * <strong>Spring Boot Concept:</strong> Concrete exception in the
 * {@code admin} domain using the static factory method pattern with
 * a private constructor.
 *
 * <p>Thrown when an administrator attempts to block or unblock their own
 * account. Prevents administrators from disabling their own access, which
 * would create an unrecoverable lockout scenario.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class AdminCannotBlockSelfException extends ShoppiqException {

    /**
     * Creates an exception for an admin attempting to block their own account.
     *
     * @return a new exception instance
     */
    public static AdminCannotBlockSelfException block() {
        return new AdminCannotBlockSelfException(
                ErrorCode.AUTH_BLOCK_SELF,
                "Administrators cannot disable their own account."
        );
    }

    /**
     * Creates an exception for an admin attempting to unblock their own account.
     *
     * @return a new exception instance
     */
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
