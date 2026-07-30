package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.request.AdminMailRequest;
import com.pkmprojects.shoppiq.email.EmailService;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.entity.newsletter.NewsletterSubscriber;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.business.InvalidRequestException;
import com.pkmprojects.shoppiq.repository.newsletter.NewsletterSubscriberRepository;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for sending single or bulk admin emails to users and newsletter subscribers.
 *
 * <p>Handles recipient deduplication, async batch processing with delays,
 * and notification preference bypass for critical admin mails.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Service
public class AdminMailService {

    private static final int BATCH_SIZE = 50;
    private static final long BATCH_DELAY_MS = 2000;
    private static final int PAGE_SIZE = 200;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final NewsletterSubscriberRepository subscriberRepository;
    @Lazy
    private final AdminMailService self;

    public AdminMailService(EmailService emailService, UserRepository userRepository,
                            NewsletterSubscriberRepository subscriberRepository,
                            @Lazy AdminMailService self) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.subscriberRepository = subscriberRepository;
        this.self = self;
    }

    /**
     * Sends an email from admin to a user or all users.
     *
     * <p>Single-recipient emails are sent synchronously.
     * Bulk emails ("send to all") are dispatched asynchronously in batches
     * so the HTTP response returns immediately.</p>
     *
     * @param request the mail request containing recipient, subject, body, and email type
     */
    public void sendMail(AdminMailRequest request, String adminEmail) {
        if (!Boolean.TRUE.equals(request.sendToAll()) && (request.toEmail() == null || request.toEmail().isBlank())) {
            throw InvalidRequestException.detail(
                    "Recipient email is required when not sending to all users.");
        }

        EmailType emailType = resolveEmailType(request.emailType());
        String templateName = emailType.getTemplateName();

        if (Boolean.TRUE.equals(request.sendToAll())) {
            self.sendToAllUsersAsync(request, emailType, templateName, adminEmail);
        } else {
            Long userId = userRepository.findUserByEmail(request.toEmail())
                    .map(User::getId).orElse(null);
            sendSingleEmail(request.toEmail(), request.subject(), request.body(), emailType, templateName, userId, "/profile");
        }
    }

    @Async
    public void sendToAllUsersAsync(AdminMailRequest request, EmailType emailType, String templateName, String adminEmail) {
        log.info("Starting async bulk mail: type={}, subject='{}'", emailType, request.subject());

        String adminEmailLower = adminEmail != null ? adminEmail.toLowerCase() : null;

        // Collect all registered user emails for deduplication against newsletter subscribers
        Set<String> registeredEmails = new HashSet<>();
        List<User> batch = new ArrayList<>(BATCH_SIZE);
        int sent = 0;
        int failed = 0;

        int pageNumber = 0;
        Page<User> userPage;
        do {
            userPage = userRepository.findAll(PageRequest.of(pageNumber, PAGE_SIZE));
            for (User user : userPage.getContent()) {
                registeredEmails.add(user.getEmail().toLowerCase());

                if (adminEmailLower != null && adminEmailLower.equals(user.getEmail().toLowerCase())) {
                    continue;
                }

                batch.add(user);
                if (batch.size() >= BATCH_SIZE) {
                    int[] result = processUserBatch(batch, request, emailType, templateName);
                    sent += result[0];
                    failed += result[1];
                    batch.clear();
                    delay();
                }
            }
            pageNumber++;
        } while (userPage.hasNext());

        if (!batch.isEmpty()) {
            int[] result = processUserBatch(batch, request, emailType, templateName);
            sent += result[0];
            failed += result[1];
            batch.clear();
        }

        List<NewsletterSubscriber> activeSubscribers = subscriberRepository.findAllByActiveTrue();
        List<NewsletterSubscriber> nonRegisteredSubscribers = activeSubscribers.stream()
                .filter(s -> !registeredEmails.contains(s.getEmail().toLowerCase()))
                .filter(s -> adminEmailLower == null || !adminEmailLower.equals(s.getEmail().toLowerCase()))
                .toList();

        List<NewsletterSubscriber> subBatch = new ArrayList<>(BATCH_SIZE);
        int subSent = 0;
        int subFailed = 0;
        for (NewsletterSubscriber subscriber : nonRegisteredSubscribers) {
            subBatch.add(subscriber);
            if (subBatch.size() >= BATCH_SIZE) {
                int[] result = processSubscriberBatch(subBatch, request, emailType, templateName);
                subSent += result[0];
                subFailed += result[1];
                subBatch.clear();
                delay();
            }
        }
        if (!subBatch.isEmpty()) {
            int[] result = processSubscriberBatch(subBatch, request, emailType, templateName);
            subSent += result[0];
            subFailed += result[1];
        }

        log.info("Bulk mail completed: users(sent={}, failed={}), subscribers(sent={}, failed={}), type={}",
                sent, failed, subSent, subFailed, emailType);
    }

    private int[] processUserBatch(List<User> batch, AdminMailRequest request, EmailType emailType, String templateName) {
        int sent = 0;
        int failed = 0;
        for (User user : batch) {
            try {
                sendSingleEmail(user.getEmail(), request.subject(), request.body(), emailType, templateName, user.getId(), "/profile");
                sent++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to send mail to {}: {}", user.getEmail(), e.getMessage());
            }
        }
        return new int[]{sent, failed};
    }

    private int[] processSubscriberBatch(List<NewsletterSubscriber> batch, AdminMailRequest request, EmailType emailType, String templateName) {
        int sent = 0;
        int failed = 0;
        for (NewsletterSubscriber subscriber : batch) {
            try {
                String unsubscribeUrl = "/api/newsletter/unsubscribe?token=" + subscriber.getToken();
                sendSingleEmail(subscriber.getEmail(), request.subject(), request.body(), emailType, templateName, null, unsubscribeUrl);
                sent++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to send mail to subscriber {}: {}", subscriber.getEmail(), e.getMessage());
            }
        }
        return new int[]{sent, failed};
    }

    private void sendSingleEmail(String toEmail, String subject, String body, EmailType emailType, String templateName, Long userId, String unsubscribeUrl) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", "Customer");
        vars.put("title", subject);
        vars.put("body", body);
        vars.put("unsubscribeUrl", unsubscribeUrl != null ? unsubscribeUrl : "/profile");

        EmailMessage message = EmailMessage.builder()
                .to(toEmail)
                .subject(subject)
                .templateName(templateName)
                .emailType(emailType)
                .userId(userId)
                .variables(vars)
                .build();

        // ADMIN_MAIL bypasses preferences (always sent)
        // PROMOTION / REVIEW_ENGAGEMENT respect user notification preferences
        if (emailType == EmailType.ADMIN_MAIL) {
            emailService.sendCriticalEmail(message);
        } else {
            emailService.sendEmail(message);
        }

        log.debug("Mail sent to {}, type: {}", toEmail, emailType);
    }

    /**
     * Pauses the current thread between batches to avoid overwhelming the SMTP server.
     *
     * <p>Called only from {@link #sendToAllUsersAsync} which runs on an async
     * thread, so blocking here does <em>not</em> hold up any request thread.</p>
     */
    private void delay() {
        try {
            Thread.sleep(BATCH_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Bulk mail batch delay interrupted");
        }
    }

    /**
     * Searches users by name, email, username, or ID.
     *
     * @param query search term
     * @return list of matching users
     */
    public List<User> searchUsers(String query) {
        if (query.matches("\\d+")) {
            return userRepository.findById(Long.parseLong(query))
                    .map(List::of)
                    .orElse(List.of());
        }
        var pageable = Pageable.ofSize(100);
        var byName = userRepository.findByNameContainingIgnoreCase(query, pageable);
        var byEmail = userRepository.findByEmailContainingIgnoreCase(query, pageable);
        var byUsername = userRepository.findByUsernameContainingIgnoreCase(query, pageable);

        return Stream.of(byName, byEmail, byUsername)
                .flatMap(page -> page.getContent().stream())
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a))
                .values()
                .stream()
                .toList();
    }

    private EmailType resolveEmailType(String emailType) {
        if (emailType == null || emailType.isBlank()) {
            return EmailType.ADMIN_MAIL;
        }
        try {
            return EmailType.valueOf(emailType.toUpperCase());
        } catch (IllegalArgumentException _) {
            return EmailType.ADMIN_MAIL;
        }
    }
}
