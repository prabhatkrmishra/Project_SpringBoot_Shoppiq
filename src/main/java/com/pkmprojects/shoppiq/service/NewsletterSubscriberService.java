package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.newsletter.NewsletterSubscribeRequest;

import java.util.List;

/**
 * Business contract for managing newsletter subscribers.
 *
 * @author PrabhatKrMishra
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
