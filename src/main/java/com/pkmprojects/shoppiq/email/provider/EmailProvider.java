package com.pkmprojects.shoppiq.email.provider;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;

/**
 * Strategy interface for email provider integrations.
 *
 * <p>
 * Each implementation handles email delivery for a specific provider
 * (SMTP, SendGrid, Console). New providers can be added without
 * modifying the email service.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface EmailProvider {

    /**
     * Returns the provider name identifier.
     *
     * @return provider name
     */
    String getProviderName();

    /**
     * Sends an email message using this provider.
     *
     * @param message the email message to send
     * @throws com.pkmprojects.shoppiq.exception.EmailSendException if sending fails
     */
    void send(EmailMessage message);

    /**
     * Returns whether this provider is currently enabled.
     *
     * @return true if enabled
     */
    boolean isEnabled();
}
