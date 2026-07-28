package com.pkmprojects.shoppiq.email.provider;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.exception.general.email.EmailSendException;

/**
 * <strong>Spring Boot Concept:</strong> Strategy interface for email provider
 * integrations. This is the contract that allows the email subsystem to
 * support multiple providers interchangeably.
 *
 * <p>
 * Each implementation handles email delivery for a specific provider
 * (SMTP, Console). New providers can be added without modifying the email
 * service — implementation of the <strong>Open/Closed Principle</strong>
 * via the <strong>Strategy pattern</strong>.
 * </p>
 *
 * <p><strong>Educational value:</strong> This interface mirrors the
 * {@link com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayStrategy}
 * pattern — both use the Strategy pattern to abstract away infrastructure
 * details:
 * <ul>
 *   <li><strong>getProviderName()</strong> — acts as a discriminator key,
 *       used by {@link EmailProviderRegistry} to index and select the active
 *       provider, similar to how {@code PaymentGatewayStrategy.supports()}
 *       works.</li>
 *   <li><strong>send()</strong> — the core operation; implementations may
 *       be synchronous or {@code @Async} (see {@link SmtpEmailProvider}),
 *       but the contract remains the same.</li>
 *   <li><strong>isEnabled()</strong> — allows providers to self-report
 *       their availability, enabling graceful fallback in the registry.</li>
 * </ul>
 * </p>
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
