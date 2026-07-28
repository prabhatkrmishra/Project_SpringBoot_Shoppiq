package com.pkmprojects.shoppiq.service.newsletter;

import com.pkmprojects.shoppiq.dto.newsletter.NewsletterSubscribeRequest;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Business contract for managing newsletter subscribers.
 *
 * <p><strong>What the Service layer demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Interface-first design</strong> — Separating the contract (this interface)
 *       from the implementation ({@link NewsletterSubscriberServiceImpl}) enables loose coupling
 *       and makes it easy to swap implementations or write unit tests with mocks.</li>
 *   <li><strong>Idempotent subscribe</strong> — {@link #subscribe} is designed to silently
 *       succeed if already subscribed, and reactivate if previously unsubscribed — a common
 *       "upsert" pattern in the service layer.</li>
 *   <li><strong>Token-based unsubscribe</strong> — {@link #unsubscribe} uses a unique token
 *       instead of authentication, a secure pattern for public-facing "one-click unsubscribe"
 *       links in email footers.</li>
 * </ul>
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * Defines the <strong>Service layer</strong> contract for newsletter subscription management.
 * Architecture: {@code NewsletterController → NewsletterSubscriberService → NewsletterSubscriberRepository}.
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *   <li>Subscribe emails — idempotent: if already subscribed, silently succeeds;
 *       if previously unsubscribed, reactivates.</li>
 *   <li>Unsubscribe via token — enables secure one-click unsubscribe without authentication.</li>
 *   <li>Retrieve active subscriber emails for bulk email campaigns.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface NewsletterSubscriberService {

    /**
     * Subscribes an email to the newsletter.
     *
     * <p>If the email is already subscribed and active, returns success silently.
     * If the email was previously unsubscribed, reactivates the subscription.</p>
     *
     * @param request the subscribe request containing the email
     */
    void subscribe(NewsletterSubscribeRequest request);

    /**
     * Unsubscribes an email using the provided token.
     *
     * @param token the unique unsubscribe token
     */
    void unsubscribe(String token);

    /**
     * Returns all active subscriber emails.
     *
     * @return list of active subscriber email addresses
     */
    List<String> getActiveSubscriberEmails();
}
