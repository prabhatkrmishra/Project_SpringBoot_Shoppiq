package com.pkmprojects.shoppiq.email;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;

/**
 * Business contract for sending transactional emails.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface EmailService {

    /**
     * Sends an email using the configured provider.
     *
     * <p>
     * The email is sent asynchronously and logged to the email_logs table.
     * If the user has disabled the corresponding notification preference,
     * the email is not sent.
     * </p>
     *
     * @param message the email message to send
     */
    void sendEmail(EmailMessage message);

    /**
     * Sends an email regardless of user notification preferences.
     *
     * <p>
     * Use this for critical security emails (password reset, verification)
     * that should always be delivered.
     * </p>
     *
     * @param message the email message to send
     */
    void sendCriticalEmail(EmailMessage message);
}
