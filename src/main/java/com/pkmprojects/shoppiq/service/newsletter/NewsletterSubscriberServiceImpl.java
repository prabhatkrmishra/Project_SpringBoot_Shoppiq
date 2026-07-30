package com.pkmprojects.shoppiq.service.newsletter;

import com.pkmprojects.shoppiq.dto.newsletter.NewsletterSubscribeRequest;
import com.pkmprojects.shoppiq.entity.newsletter.NewsletterSubscriber;
import com.pkmprojects.shoppiq.exception.business.InvalidRequestException;
import com.pkmprojects.shoppiq.repository.newsletter.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * {@link NewsletterSubscriberService} implementation handling idempotent subscribe,
 * soft-delete unsubscribe with reactivation, and active subscriber email retrieval.
 *
 * @author prabhatkrmishra
 * @see NewsletterSubscriberService
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
        Objects.requireNonNull(request, "Newsletter subscribe request must not be null.");

        String email = request.email().trim().toLowerCase();

        if (email.isBlank()) {
            throw InvalidRequestException.detail("Email must not be blank.");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw InvalidRequestException.detail("Email format is invalid.");
        }

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
