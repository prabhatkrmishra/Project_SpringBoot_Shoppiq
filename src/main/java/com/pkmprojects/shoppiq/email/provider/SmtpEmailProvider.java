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
 * <strong>Spring Boot Concept:</strong> Implementation of {@link EmailProvider}
 * using Spring's {@link JavaMailSender} for SMTP-based email delivery.
 * The default {@code shoppiq.email.provider=smtp} provider for production
 * environments.
 *
 * <p>
 * Default provider for production environments. Supports HTML email
 * rendering via Thymeleaf templates.
 * </p>
 *
 * <p><strong>Educational value:</strong> This class demonstrates several
 * Spring Boot infrastructure patterns:
 * <ul>
 *   <li><strong>JavaMailSender</strong> — Spring's abstraction over
 *       {@code jakarta.mail}, auto-configured by {@code spring-boot-starter-mail}.
 *       Adding {@code spring.mail.host}, {@code spring.mail.username}, etc.
 *       to application.properties is all that's needed to configure it.</li>
 *   <li><strong>@Async method</strong> — the {@code send()} method is
 *       annotated with Spring's {@code @Async}, meaning it runs on a separate
 *       thread pool (configured via {@code @EnableAsync} on a configuration
 *       class). This ensures that the calling thread (usually a service layer
 *       method) is not blocked by the SMTP call.</li>
 *   <li><strong>Thymeleaf for email</strong> — uses a dedicated
 *       {@link org.thymeleaf.TemplateEngine} (injected with
 *       {@code @Qualifier("emailTemplateEngine")}) for rendering HTML
 *       email templates, separate from the web-facing template engine.</li>
 *   <li><strong>MimeMessageHelper</strong> — Spring's convenience wrapper
 *       for building Jakarta Mail {@code MimeMessage}s, supporting HTML
 *       content and attachments.</li>
 *   <li><strong>Graceful disabling</strong> — checks {@code shoppiq.email.enabled}
 *       at runtime, allowing operators to disable email without redeploying.</li>
 * </ul>
 * </p>
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
