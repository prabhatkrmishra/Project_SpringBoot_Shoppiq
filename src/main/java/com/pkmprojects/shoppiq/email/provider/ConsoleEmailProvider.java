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
 * Console email provider for development and testing.
 *
 * <p>
 * Logs email content to the console instead of sending actual emails.
 * Useful for local development and integration tests.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "shoppiq.email.provider", havingValue = "console")
public class ConsoleEmailProvider implements EmailProvider {

    private final TemplateEngine emailTemplateEngine;

    @Value("${shoppiq.email.from:noreply@shoppiq.com}")
    private String fromAddress;

    public ConsoleEmailProvider(@Qualifier("emailTemplateEngine") TemplateEngine emailTemplateEngine) {
        this.emailTemplateEngine = emailTemplateEngine;
    }

    @Override
    public String getProviderName() {
        return "CONSOLE";
    }

    @Override
    public void send(EmailMessage message) {
        String htmlContent = renderTemplate(message.getTemplateName(), message.getVariables());

        log.info("========== EMAIL (Console Provider) ==========");
        log.info("From: {}", fromAddress);
        log.info("To: {}", message.getTo());
        log.info("Subject: {}", message.getSubject());
        log.info("Type: {}", message.getEmailType());
        log.info("User ID: {}", message.getUserId());
        log.info("Template: {}", message.getTemplateName());
        log.info("--- HTML Content ---");
        log.info(htmlContent);
        log.info("==============================================");
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
        return emailTemplateEngine.process("emails/" + templateName, context);
    }
}
