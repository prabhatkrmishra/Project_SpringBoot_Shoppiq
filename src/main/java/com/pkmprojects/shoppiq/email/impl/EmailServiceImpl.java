package com.pkmprojects.shoppiq.email.impl;

import com.pkmprojects.shoppiq.email.EmailService;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.email.entity.EmailLog;
import com.pkmprojects.shoppiq.email.entity.EmailLog.EmailStatus;
import com.pkmprojects.shoppiq.email.provider.EmailProvider;
import com.pkmprojects.shoppiq.email.provider.EmailProviderRegistry;
import com.pkmprojects.shoppiq.email.repository.EmailLogRepository;
import com.pkmprojects.shoppiq.entity.notification.NotificationPreference;
import com.pkmprojects.shoppiq.exception.general.email.EmailSendException;
import com.pkmprojects.shoppiq.repository.notification.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Service-layer implementation
 * ({@code @Service}) of {@link EmailService} that orchestrates email
 * delivery with preference checking, audit logging, and error handling.
 *
 * <p>
 * Handles email delivery with preference checking, logging, and error handling.
 * Critical emails (security alerts, verification, password reset) bypass
 * user notification preferences.
 * </p>
 *
 * <p><strong>Educational value:</strong> This class demonstrates the
 * <strong>Service layer</strong> in a clean layered architecture:
 * <ul>
 *   <li><strong>Dependency injection via constructor</strong> — uses
 *       Lombok {@code @RequiredArgsConstructor} (which generates a constructor
 *       for all final fields) and Spring resolves the three dependencies:
 *       {@link com.pkmprojects.shoppiq.email.provider.EmailProviderRegistry},
 *       {@link com.pkmprojects.shoppiq.email.repository.EmailLogRepository}, and
 *       {@link com.pkmprojects.shoppiq.repository.notification.NotificationPreferenceRepository}.</li>
 *   <li><strong>Repository pattern</strong> — persistence concerns are
 *       delegated to repositories, keeping the service focused on
 *       orchestration and business rules.</li>
 *   <li><strong>Strategy delegation</strong> — the actual email sending is
 *       delegated to the active {@link com.pkmprojects.shoppiq.email.provider.EmailProvider}
 *       resolved from the registry. The service doesn't know whether it's
 *       using SMTP or Console — it just calls {@code provider.send(message)}.</li>
 *   <li><strong>Defensive error handling</strong> — both {@code EmailSendException}
 *       and generic {@code Exception} are caught separately, with different
 *       log levels, ensuring that email failures never propagate to the caller.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailProviderRegistry providerRegistry;
    private final EmailLogRepository emailLogRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final Clock clock;

    @Override
    public void sendEmail(EmailMessage message) {
        if (message.getUserId() != null && !shouldSendEmail(message.getUserId(), message.getEmailType())) {
            log.debug("Email skipped due to user preference: type={}, userId={}", message.getEmailType(), message.getUserId());
            logSkippedEmail(message);
            return;
        }

        sendWithLogging(message);
    }

    @Override
    public void sendCriticalEmail(EmailMessage message) {
        sendWithLogging(message);
    }

    private void sendWithLogging(EmailMessage message) {
        EmailProvider provider = providerRegistry.getActiveProvider();
        EmailLog emailLog = createEmailLog(message, provider.getProviderName());

        try {
            provider.send(message);
            emailLog.setStatus(EmailStatus.SENT);
            emailLog.setSentAt(Instant.now(clock));
            log.debug("Email sent: type={}, to={}, provider={}", message.getEmailType(), message.getTo(), provider.getProviderName());
        } catch (EmailSendException e) {
            emailLog.setStatus(EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            log.error("Email failed: type={}, to={}, error={}", message.getEmailType(), message.getTo(), e.getMessage());
        } catch (Exception e) {
            emailLog.setStatus(EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            log.error("Email failed unexpectedly: type={}, to={}", message.getEmailType(), message.getTo(), e);
        }

        emailLogRepository.save(emailLog);
    }

    private boolean shouldSendEmail(Long userId, EmailType emailType) {
        if (emailType == null) {
            return true;
        }

        NotificationPreference preference = preferenceRepository.findByUserId(userId).orElse(null);
        if (preference == null) {
            return true;
        }

        return switch (emailType) {
            case ORDER_UPDATE -> preference.isOrderUpdates();
            case SECURITY_ALERT, PASSWORD_RESET, VERIFICATION -> preference.isAccountSecurity();
            case PROMOTION -> preference.isPromotions();
            case REVIEW_ENGAGEMENT -> preference.isReviewsEngagement();
            case WELCOME, ADMIN_MAIL -> true;
        };
    }

    private EmailLog createEmailLog(EmailMessage message, String providerName) {
        return EmailLog.builder()
                .emailType(message.getEmailType())
                .recipientEmail(message.getTo())
                .subject(message.getSubject())
                .status(EmailStatus.PENDING)
                .provider(providerName)
                .build();
    }

    private void logSkippedEmail(EmailMessage message) {
        EmailLog emailLog = createEmailLog(message, "SKIPPED");
        emailLog.setStatus(EmailStatus.PENDING);
        emailLogRepository.save(emailLog);
    }
}
