package com.pkmprojects.shoppiq.exception.general.email;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when an email fails
 * to send.
 *
 * <p>Extends {@link com.pkmprojects.shoppiq.exception.base.ShoppiqException}
 * directly (not a business exception subclass) because email failures are
 * integration errors, not business logic violations. Uses HTTP 502
 * (Bad Gateway) to indicate the failure originates from an upstream service.
 * The constructor accepting {@link Throwable} preserves the root cause
 * (e.g., network timeout, SMTP auth failure) for debugging.</p>
 *
 * @author prabhatkrmishra
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
