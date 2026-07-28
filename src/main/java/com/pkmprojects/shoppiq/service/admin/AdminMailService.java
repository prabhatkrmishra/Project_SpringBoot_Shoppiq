package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.request.AdminMailRequest;
import com.pkmprojects.shoppiq.email.EmailService;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.entity.newsletter.NewsletterSubscriber;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.repository.newsletter.NewsletterSubscriberRepository;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <strong>Spring Boot Concept:</strong> Service for admin mail functionality.
 *
 * <h2>What is {@code @Service}?</h2>
 * <p>
 * {@code @Service} is a Spring Stereotype annotation. It registers this class as a Spring bean,
 * making it available for injection into controllers (e.g., {@code AdminMailController}).
 * It semantically marks this class as part of the <strong>Service layer</strong> in the
 * layered architecture.
 * </p>
 *
 * <h2>Why no {@code @Transactional} here?</h2>
 * <p>
 * This service does not directly interact with a single logical database transaction for its
 * primary operations. Email sending is an external side effect (SMTP), and the data reads
 * (user/subscriber lookups) are simple lookups. The {@code @Transactional} annotation is
 * omitted because:
 * <ul>
 *   <li>Sending emails involves external I/O, not database writes.</li>
 *   <li>Batch processing uses {@code @Async} for non-blocking execution.</li>
 *   <li>Transactional boundaries are managed by the calling service or controller.</li>
 * </ul>
 * This is a valid exception to the "always use {@code @Transactional}" guideline because
 * the service's primary responsibility is <strong>sending emails</strong>, not coordinating
 * database writes.
 * </p>
 *
 * <h2>Constructor Injection (Dependency Injection Pattern)</h2>
 * <p>
 * Dependencies ({@code EmailService}, {@code UserRepository}, etc.) are injected via the
 * constructor. The {@code @Lazy} annotation on the self-reference prevents circular dependency
 * issues when calling the {@code @Async} method internally.
 * </p>
 *
 * <h2>Role in Layered Architecture</h2>
 * <pre>
 * AdminMailController → AdminMailService → EmailService (SMTP)
 *     (HTTP/REST)          (mail logic)         (email delivery)
 *                            ↕
 *                     UserRepository
 *                     NewsletterSubscriberRepository
 * </pre>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *   <li>Send single or bulk emails from admin to users/subscribers.</li>
 *   <li>Deduplicate registered users vs newsletter subscribers to avoid double-sending.</li>
 *   <li>Use {@code @Async} for bulk sending so the HTTP response returns immediately.</li>
 *   <li>Batch processing with delays to avoid overwhelming the SMTP server.</li>
 *   <li>Respect notification preferences (critical admin mails bypass preferences).</li>
 *   <li>Search users by name, email, username, or ID for recipient selection.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Service
public class AdminMailService {

    private static final int BATCH_SIZE = 50;
    private static final long BATCH_DELAY_MS = 2000;

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

    private static final int PAGE_SIZE = 200;

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
        List<User> byName = userRepository.findByNameContainingIgnoreCase(query);
        List<User> byEmail = userRepository.findByEmailContainingIgnoreCase(query);
        List<User> byUsername = userRepository.findByUsernameContainingIgnoreCase(query);

        return Stream.of(byName, byEmail, byUsername)
                .flatMap(List::stream)
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
