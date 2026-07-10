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
@RequiredArgsConstructor
public class AdminMailService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final NewsletterSubscriberRepository subscriberRepository;

    /**
     * Sends an email from admin to a user or all users.
     *
     * @param request the mail request containing recipient, subject, body, and email type
     */
    public void sendMail(AdminMailRequest request) {
        EmailType emailType = resolveEmailType(request.emailType());
        String templateName = emailType.getTemplateName();

        if (Boolean.TRUE.equals(request.sendToAll())) {
            sendToAllUsers(request, emailType, templateName);
        } else {
            Long userId = userRepository.findUserByEmail(request.toEmail())
                    .map(User::getId).orElse(null);
            sendSingleEmail(request.toEmail(), request.subject(), request.body(), emailType, templateName, userId, "/profile");
        }
    }

    private void sendToAllUsers(AdminMailRequest request, EmailType emailType, String templateName) {
        List<User> allUsers = userRepository.findAll();
        int sent = 0;
        int skipped = 0;

        for (User user : allUsers) {
            try {
                sendSingleEmail(user.getEmail(), request.subject(), request.body(), emailType, templateName, user.getId(), "/profile");
                sent++;
            } catch (Exception e) {
                skipped++;
                log.error("Failed to send admin mail to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        Set<String> registeredEmails = allUsers.stream()
                .map(User::getEmail)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<NewsletterSubscriber> activeSubscribers = subscriberRepository.findAllByActiveTrue();
        int subscribersSent = 0;
        int subscribersSkipped = 0;

        for (NewsletterSubscriber subscriber : activeSubscribers) {
            if (registeredEmails.contains(subscriber.getEmail().toLowerCase())) {
                continue;
            }
            try {
                String unsubscribeUrl = "/api/newsletter/unsubscribe?token=" + subscriber.getToken();
                sendSingleEmail(subscriber.getEmail(), request.subject(), request.body(), emailType, templateName, null, unsubscribeUrl);
                subscribersSent++;
            } catch (Exception e) {
                subscribersSkipped++;
                log.error("Failed to send admin mail to newsletter subscriber {}: {}", subscriber.getEmail(), e.getMessage());
            }
        }

        log.info("Admin bulk mail completed: users(sent={}, skipped={}), subscribers(sent={}, skipped={}), type={}",
                sent, skipped, subscribersSent, subscribersSkipped, emailType);
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

        log.info("Admin mail sent to {}, subject: {}, type: {}", toEmail, subject, emailType);
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
