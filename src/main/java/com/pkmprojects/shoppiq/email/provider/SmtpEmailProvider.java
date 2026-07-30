package com.pkmprojects.shoppiq.email.provider;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.exception.general.email.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * SMTP-based email provider using Spring's JavaMailSender for production environments.
 *
 * <p>Default provider with HTML email rendering via Thymeleaf templates and async delivery.
 * This provider sends emails through an SMTP server configured via Spring's mail properties.
 * It renders email content using Thymeleaf templates and sends HTML-formatted emails with
 * UTF-8 encoding. The provider is marked with {@code @Async} to ensure non-blocking email
 * delivery.</p>
 *
 * <p>The provider can be disabled via the {@code shoppiq.email.enabled} property. When disabled,
 * emails are silently skipped without attempting delivery. This is useful for environments where
 * email delivery should be simulated without actually sending messages.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Component
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;
    private final String fromAddress;
    private final boolean enabled;

    public SmtpEmailProvider(JavaMailSender mailSender,
                             @Qualifier("emailTemplateEngine") TemplateEngine emailTemplateEngine,
                             @Value("${shoppiq.email.from:noreply@shoppiq.com}") String fromAddress,
                             @Value("${shoppiq.email.enabled:true}") boolean enabled) {
        this.mailSender = mailSender;
        this.emailTemplateEngine = emailTemplateEngine;
        this.fromAddress = fromAddress;
        this.enabled = enabled;
    }

    @Override
    public String getProviderName() {
        return "SMTP";
    }

    @Override
    @Async
    public void send(EmailMessage message) {
        if (!enabled) {
            log.debug("Email sending disabled, skipping email to {}", message.getTo());
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());

            String htmlContent = renderTemplate(message.getTemplateName(), message.getVariables());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.debug("Email sent successfully to {} via SMTP, type={}", message.getTo(), message.getEmailType());
        } catch (MessagingException e) {
            log.error("Failed to send email to {} via SMTP: {}", message.getTo(), e.getMessage(), e);
            throw new EmailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        return emailTemplateEngine.process(templateName, context);
    }
}
