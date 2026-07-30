package com.pkmprojects.shoppiq.repository.newsletter;

import com.pkmprojects.shoppiq.entity.newsletter.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for the {@link NewsletterSubscriber} aggregate.
 *
 * <p>Provides methods to query newsletter subscribers by email, token, and active status for
 * newsletter management. The repository supports existence checks for subscription validation,
 * token-based unsubscribe lookups, and bulk queries for email distribution.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    /**
     * Finds a subscriber by email (case-insensitive lookup).
     *
     * @param email the subscriber email
     * @return optional subscriber
     */
    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);

    /**
     * Finds a subscriber by their unsubscribe token.
     *
     * @param token the unsubscribe token
     * @return optional subscriber
     */
    Optional<NewsletterSubscriber> findByToken(String token);

    /**
     * Checks whether a subscriber with the given email exists.
     *
     * @param email the subscriber email
     * @return true if a subscriber with this email exists
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Returns all active newsletter subscribers.
     *
     * @return list of active subscribers
     */
    List<NewsletterSubscriber> findAllByActiveTrue();

    /**
     * Returns all active subscriber email addresses.
     *
     * @return list of email strings
     */
    @Query("SELECT ns.email FROM NewsletterSubscriber ns WHERE ns.active = true")
    List<String> findAllActiveEmails();
}
