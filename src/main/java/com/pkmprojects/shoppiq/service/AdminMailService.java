package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.admin.request.AdminMailRequest;
import com.pkmprojects.shoppiq.email.EmailService;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.entity.NewsletterSubscriber;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.repository.NewsletterSubscriberRepository;
import com.pkmprojects.shoppiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for admin mail functionality.
 *
 * @author PrabhatKrMishra
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

    @Async
    public void sendToAllUsersAsync(AdminMailRequest request, EmailType emailType, String templateName, String adminEmail) {
        log.info("Starting async bulk mail: type={}, subject='{}'", emailType, request.subject());

        String adminEmailLower = adminEmail != null ? adminEmail.toLowerCase() : null;

        List<User> allUsers = userRepository.findAll();
        if (adminEmailLower != null) {
            allUsers = allUsers.stream()
                    .filter(u -> !adminEmailLower.equals(u.getEmail().toLowerCase()))
                    .toList();
        }
        int sent = 0;
        int failed = 0;

        List<User> batch = new ArrayList<>(BATCH_SIZE);
        for (User user : allUsers) {
            batch.add(user);
            if (batch.size() >= BATCH_SIZE) {
                int[] result = processUserBatch(batch, request, emailType, templateName);
                sent += result[0];
                failed += result[1];
                batch.clear();
                delay();
            }
        }
        if (!batch.isEmpty()) {
            int[] result = processUserBatch(batch, request, emailType, templateName);
            sent += result[0];
            failed += result[1];
            batch.clear();
        }

        Set<String> registeredEmails = allUsers.stream()
                .map(User::getEmail)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

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

    private void delay() {
        try {
            Thread.sleep(BATCH_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Bulk mail batch delay interrupted");
        }
    }

    private EmailType resolveEmailType(String emailType) {
        if (emailType == null || emailType.isBlank()) {
            return EmailType.ADMIN_MAIL;
        }
        try {
            return EmailType.valueOf(emailType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EmailType.ADMIN_MAIL;
        }
    }
}
