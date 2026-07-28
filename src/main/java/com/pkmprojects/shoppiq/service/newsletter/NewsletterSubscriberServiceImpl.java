package com.pkmprojects.shoppiq.service.newsletter;

import com.pkmprojects.shoppiq.dto.newsletter.NewsletterSubscribeRequest;
import com.pkmprojects.shoppiq.entity.newsletter.NewsletterSubscriber;
import com.pkmprojects.shoppiq.repository.newsletter.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * <strong>Spring Boot Concept:</strong> Default implementation of {@link NewsletterSubscriberService}.
 *
 * <p><strong>What this Service implementation demonstrates:</strong></p>
 * <ul>
 *   <li><strong>@Transactional</strong> — Write operations ({@link #subscribe}, {@link #unsubscribe})
 *       are wrapped in transactions so that entity changes propagate to the database atomically.
 *       Read operations ({@link #getActiveSubscriberEmails}) use {@code readOnly = true} for
 *       a minor performance hint to the persistence provider.</li>
 *   <li><strong>Idempotent subscribe</strong> — The subscribe method checks if the email exists.
 *       If active, it silently returns (no-op). If inactive, it reactivates. This avoids
 *       IllegalArgumentException for duplicate subscriptions.</li>
 *   <li><strong>Soft-delete unsubscribe</strong> — Instead of deleting the row, the subscriber
 *       is marked inactive and the {@code unsubscribedAt} timestamp is recorded. This preserves
 *       the subscription history and allows reactivation.</li>
 *   <li><strong>Constructor injection with {@code @RequiredArgsConstructor}</strong> — Dependencies
 *       ({@code NewsletterSubscriberRepository}, {@code Clock}) are injected via constructor,
 *       which is the Spring-recommended approach for immutability and testability.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterSubscriberServiceImpl implements NewsletterSubscriberService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final Clock clock;

    /**
     * Subscribes an email to the newsletter, reactivating if previously unsubscribed.
     *
     * <p>If the email is already active, this is a no-op. If inactive, it
     * reactivates and clears the unsubscribed timestamp.</p>
     *
     * @param request subscription request containing the email address
     */
    @Override
    @Transactional
    public void subscribe(NewsletterSubscribeRequest request) {
        String email = request.email().trim().toLowerCase();

        var existing = subscriberRepository.findByEmailIgnoreCase(email);

        if (existing.isPresent()) {
            NewsletterSubscriber subscriber = existing.get();
            if (subscriber.isActive()) {
                log.debug("Newsletter subscription already active for {}", email);
                return;
            }
            subscriber.setActive(true);
            subscriber.setSubscribedAt(clock.instant());
            subscriber.setUnsubscribedAt(null);
            subscriberRepository.save(subscriber);
            log.debug("Newsletter subscription reactivated for {}", email);
            return;
        }

        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email(email)
                .token(UUID.randomUUID().toString())
                .active(true)
                .subscribedAt(clock.instant())
                .build();
        subscriberRepository.save(subscriber);
        log.debug("New newsletter subscription for {}", email);
    }

    /**
     * Unsubscribes using the unique token, performing a soft-delete.
     *
     * <p>Marks the subscriber as inactive and records the unsubscribed timestamp
     * instead of deleting the row, preserving subscription history.</p>
     *
     * @param token the unique unsubscribe token
     * @throws IllegalArgumentException if the token is invalid
     */
    @Override
    @Transactional
    public void unsubscribe(String token) {
        NewsletterSubscriber subscriber = subscriberRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid unsubscribe link."));

        if (!subscriber.isActive()) {
            log.debug("Newsletter subscription already inactive for {}", subscriber.getEmail());
            return;
        }

        subscriber.setActive(false);
        subscriber.setUnsubscribedAt(clock.instant());
        subscriberRepository.save(subscriber);
        log.debug("Newsletter subscription removed for {}", subscriber.getEmail());
    }

    /**
     * Returns the email addresses of all active subscribers.
     *
     * @return list of active subscriber email addresses
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveSubscriberEmails() {
        return subscriberRepository.findAllActiveEmails();
    }
}
