package com.pkmprojects.shoppiq.exception.general.verification;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a verification code is invalid, expired, or has exceeded max attempts.
 *
 * <p>This exception is thrown during the verification code validation flow
 * when the submitted code does not match any active verification code in
 * the database, has expired, or the user has exceeded the maximum number
 * of verification attempts. It uses the appropriate {@link ErrorCode}
 * (INVALID_CODE, EXPIRED, or MAX_ATTEMPTS_EXCEEDED) and HTTP 400 Bad
 * Request status.</p>
 *
 * <p>The detail message should explain the specific failure reason and
 * guide the user on next steps (e.g., "Invalid verification code.",
 * "Verification code has expired. Please request a new code.", or
 * "Maximum verification attempts exceeded. Please request a new code.").</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#VERIFICATION_CODE_INVALID
 * @see ErrorCode#VERIFICATION_CODE_EXPIRED
 * @since 1.0.0
 */
public final class VerificationCodeException extends ShoppiqException {

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
