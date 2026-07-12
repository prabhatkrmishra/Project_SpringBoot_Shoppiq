package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.newsletter.NewsletterSubscribeRequest;
import com.pkmprojects.shoppiq.entity.NewsletterSubscriber;
import com.pkmprojects.shoppiq.repository.NewsletterSubscriberRepository;
import com.pkmprojects.shoppiq.service.NewsletterSubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link NewsletterSubscriberService}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterSubscriberServiceImpl implements NewsletterSubscriberService {

    private final NewsletterSubscriberRepository subscriberRepository;

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
            subscriber.setSubscribedAt(Instant.now());
            subscriber.setUnsubscribedAt(null);
            subscriberRepository.save(subscriber);
            log.debug("Newsletter subscription reactivated for {}", email);
            return;
        }

        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email(email)
                .token(UUID.randomUUID().toString())
                .active(true)
                .subscribedAt(Instant.now())
                .build();
        subscriberRepository.save(subscriber);
        log.debug("New newsletter subscription for {}", email);
    }

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
        subscriber.setUnsubscribedAt(Instant.now());
        subscriberRepository.save(subscriber);
        log.debug("Newsletter subscription removed for {}", subscriber.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveSubscriberEmails() {
        return subscriberRepository.findAllActiveEmails();
    }
}
