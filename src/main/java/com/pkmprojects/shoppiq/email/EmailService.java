package com.pkmprojects.shoppiq.email;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;

/**
 * Service-layer interface defining the business contract for sending transactional emails.
 *
 * <p>Provides methods for standard email delivery with preference checking and critical email delivery that bypasses preferences.
 * Standard emails respect user notification preferences, allowing users to opt out of non-essential
 * communications. Critical emails bypass preferences to ensure delivery of security-sensitive
 * messages such as password resets and account verification codes.</p>
 *
 * <p>All email operations are logged to the email_logs table for auditing and debugging purposes.
 * The service delegates to the configured email provider (SMTP, console, etc.) through the
 * EmailProviderRegistry, supporting multiple email backends for different environments.</p>
 *
 * @author prabhatkrmishra
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
