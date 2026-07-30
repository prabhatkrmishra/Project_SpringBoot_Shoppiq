package com.pkmprojects.shoppiq.exception.general.email;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an email fails to send through the configured provider.
 *
 * <p>This exception is thrown when the email service encounters an error
 * while attempting to deliver a message. This may be due to SMTP
 * connectivity issues, invalid recipient addresses, or provider rate
 * limits. It uses the {@link ErrorCode#EMAIL_SEND_FAILED} code and
 * HTTP 502 Bad Gateway status. The actual error is logged for
 * diagnostics.</p>
 *
 * <p>The detail message should explain the failure reason (e.g.,
 * "Failed to send email: Connection refused") to help diagnose the
 * email delivery issue. The client should retry the operation or
 * contact support if the issue persists.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#EMAIL_SEND_FAILED
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
