package com.pkmprojects.shoppiq.email.provider;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.exception.general.email.EmailSendException;

/**
 * Strategy interface for email provider integrations.
 *
 * <p>Allows the email subsystem to support multiple providers interchangeably using the Strategy pattern.
 * Each provider implementation handles the actual email delivery mechanism (SMTP, console, etc.)
 * while the interface provides a uniform API for the email service layer. This abstraction
 * enables seamless switching between email providers based on environment configuration.</p>
 *
 * <p>The registry collects all provider beans at startup and resolves the active one based on
 * configuration. Providers can be enabled or disabled via configuration properties, allowing
 * different email backends for development, testing, and production environments.</p>
 *
 * @author prabhatkrmishra
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
     * @throws EmailSendException if sending fails
     */
    void send(EmailMessage message);

    /**
     * Returns whether this provider is currently enabled.
     *
     * @return true if enabled
     */
    boolean isEnabled();
}
