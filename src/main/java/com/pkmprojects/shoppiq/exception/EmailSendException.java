package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an email fails to send.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public class EmailSendException extends ShoppiqException {

    /**
     * Creates a new EmailSendException with a detail message.
     *
     * @param detail error description
     */
    public EmailSendException(String detail) {
        super(ErrorCode.EMAIL_SEND_FAILED, HttpStatus.BAD_GATEWAY, detail);
    }

    /**
     * Creates a new EmailSendException wrapping a cause.
     *
     * @param detail error description
     * @param cause  underlying exception
     */
    public EmailSendException(String detail, Throwable cause) {
        super(ErrorCode.EMAIL_SEND_FAILED, HttpStatus.BAD_GATEWAY, detail);
        initCause(cause);
    }
}
