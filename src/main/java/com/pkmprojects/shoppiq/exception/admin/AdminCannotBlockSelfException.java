package com.pkmprojects.shoppiq.exception.admin;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an administrator attempts to block or unblock their own account.
 *
 * <p>This exception prevents administrators from accidentally locking
 * themselves out of the system. When an admin tries to disable their own
 * account, the {@link #block()} factory method creates an exception with
 * the {@link ErrorCode#AUTH_BLOCK_SELF} code. When an admin tries to
 * enable their own account (which is redundant if already active), the
 * {@link #unblock()} factory method creates an exception with the
 * {@link ErrorCode#AUTH_UNBLOCK_SELF} code.</p>
 *
 * <p>Both cases use HTTP 403 Forbidden status. The detail messages are
 * fixed strings to avoid information leakage. Another administrator
 * must perform the block/unblock operation if it is genuinely needed.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#AUTH_BLOCK_SELF
 * @see ErrorCode#AUTH_UNBLOCK_SELF
 * @since 1.0.0
 */
public final class AdminCannotBlockSelfException extends ShoppiqException {

    private AdminCannotBlockSelfException(ErrorCode errorCode, String detail) {
        super(errorCode, HttpStatus.FORBIDDEN, detail);
    }

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
}
