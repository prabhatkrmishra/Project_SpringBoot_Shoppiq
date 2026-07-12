package com.pkmprojects.shoppiq.email.provider;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.exception.EmailSendException;
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
 * SMTP email provider using Spring's {@link JavaMailSender}.
 *
 * <p>
 * Default provider for production environments. Supports HTML email
 * rendering via Thymeleaf templates.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Slf4j
@Component
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;

    @Value("${shoppiq.email.from:noreply@shoppiq.com}")
    private String fromAddress;

    @Value("${shoppiq.email.enabled:true}")
    private boolean enabled;

    public SmtpEmailProvider(JavaMailSender mailSender,
                              @Qualifier("emailTemplateEngine") TemplateEngine emailTemplateEngine) {
        this.mailSender = mailSender;
        this.emailTemplateEngine = emailTemplateEngine;
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
        return emailTemplateEngine.process("emails/" + templateName, context);
    }
}
