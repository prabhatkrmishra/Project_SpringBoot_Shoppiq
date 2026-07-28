package com.pkmprojects.shoppiq.email;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;

/**
 * <strong>Spring Boot Concept:</strong> Service-layer interface defining the
 * business contract for sending transactional emails. This is the
 * <em>Interface Segregation</em> and <em>Dependency Inversion</em> principle
 * in action — higher layers depend on this abstraction, not on concrete
 * implementations.
 *
 * <p><strong>Educational value:</strong> In a layered Spring Boot architecture:
 * <ul>
 *   <li><strong>Controller</strong> layer handles HTTP request/response.</li>
 *   <li><strong>Service</strong> layer (this interface) defines business
 *       operations and orchestrates domain logic.</li>
 *   <li><strong>Provider</strong> layer (see {@link com.pkmprojects.shoppiq.email.provider.EmailProvider})
 *       handles infrastructure concerns (SMTP, console logging).</li>
 * </ul>
 * This interface provides two methods: {@link #sendEmail} which respects
 * user notification preferences, and {@link #sendCriticalEmail} which
 * bypasses them for security-critical messages (password reset,
 * verification). This separation of concerns is a common pattern when
 * some emails are mandatory and others are opt-out.
 * </p>
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
