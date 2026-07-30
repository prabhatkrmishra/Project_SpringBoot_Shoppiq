package com.pkmprojects.shoppiq.email.provider;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Console-based email provider that logs email content instead of sending actual emails.
 *
 * <p>Activated via the {@code shoppiq.email.provider=console} property for local development and testing.
 * This provider renders the email template and logs the content to the application logger instead
 * of sending actual emails. It is useful for development environments where no SMTP server is
 * available and for integration testing where email delivery should be verified without actually
 * sending messages.</p>
 *
 * <p>The provider uses the same Thymeleaf template engine as the SMTP provider to ensure that
 * template rendering is consistent across environments. Any template errors will be caught during
 * rendering, providing early feedback during development.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "shoppiq.email.provider", havingValue = "console")
public class ConsoleEmailProvider implements EmailProvider {

    private final TemplateEngine emailTemplateEngine;
    private final String fromAddress;

    public ConsoleEmailProvider(@Qualifier("emailTemplateEngine") TemplateEngine emailTemplateEngine,
                                @Value("${shoppiq.email.from:noreply@shoppiq.com}") String fromAddress) {
        this.emailTemplateEngine = emailTemplateEngine;
        this.fromAddress = fromAddress;
    }

    @Override
    public String getProviderName() {
        return "CONSOLE";
    }

    @Override
    public void send(EmailMessage message) {
        String htmlContent = renderTemplate(message.getTemplateName(), message.getVariables());

        log.debug("========== EMAIL (Console Provider) ==========");
        log.debug("From: {}", fromAddress);
        log.debug("To: {}", message.getTo());
        log.debug("Subject: {}", message.getSubject());
        log.debug("Type: {}", message.getEmailType());
        log.debug("User ID: {}", message.getUserId());
        log.debug("Template: {}", message.getTemplateName());
        log.debug("==============================================");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        return emailTemplateEngine.process(templateName, context);
    }
}
