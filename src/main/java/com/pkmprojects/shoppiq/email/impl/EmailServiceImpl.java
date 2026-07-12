package com.pkmprojects.shoppiq.email.impl;

import com.pkmprojects.shoppiq.email.EmailService;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.email.entity.EmailLog;
import com.pkmprojects.shoppiq.email.entity.EmailLog.EmailStatus;
import com.pkmprojects.shoppiq.email.provider.EmailProvider;
import com.pkmprojects.shoppiq.email.provider.EmailProviderRegistry;
import com.pkmprojects.shoppiq.email.repository.EmailLogRepository;
import com.pkmprojects.shoppiq.entity.NotificationPreference;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.EmailSendException;
import com.pkmprojects.shoppiq.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Default implementation of {@link EmailService}.
 *
 * <p>
 * Handles email delivery with preference checking, logging, and error handling.
 * Critical emails (security alerts, verification, password reset) bypass
 * user notification preferences.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailProviderRegistry providerRegistry;
    private final EmailLogRepository emailLogRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    @Override
    @Transactional
    public void sendEmail(EmailMessage message) {
        if (message.getUserId() != null && !shouldSendEmail(message.getUserId(), message.getEmailType())) {
            log.debug("Email skipped due to user preference: type={}, userId={}", message.getEmailType(), message.getUserId());
            logEmail(message, null, EmailStatus.PENDING);
            return;
        }

        sendWithLogging(message);
    }

    @Override
    @Transactional
    public void sendCriticalEmail(EmailMessage message) {
        sendWithLogging(message);
    }

    private void sendWithLogging(EmailMessage message) {
        EmailProvider provider = providerRegistry.getActiveProvider();
        EmailLog emailLog = createEmailLog(message, provider.getProviderName());

        try {
            provider.send(message);
            emailLog.setStatus(EmailStatus.SENT);
            emailLog.setSentAt(LocalDateTime.now());
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

    private void logEmail(EmailMessage message, String errorMessage, EmailStatus status) {
        EmailLog emailLog = createEmailLog(message, "SKIPPED");
        emailLog.setErrorMessage(errorMessage);
        emailLog.setStatus(status);
        emailLogRepository.save(emailLog);
    }
}
