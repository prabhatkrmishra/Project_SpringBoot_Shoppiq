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
 * <strong>Spring Boot Concept:</strong> Implementation of {@link EmailProvider}
 * that logs email content to the console instead of sending actual emails.
 * Activated via the {@code shoppiq.email.provider=console} configuration property.
 *
 * <p>
 * Logs email content to the console instead of sending actual emails.
 * Useful for local development and integration tests.
 * </p>
 *
 * <p><strong>Educational value:</strong> This class demonstrates several
 * Spring Boot patterns:
 * <ul>
 *   <li><strong>Conditional bean registration</strong> — {@code @ConditionalOnProperty}
 *       ensures this bean is only created when {@code shoppiq.email.provider=console}.
 *       When the property is set to {@code smtp}, this bean is not loaded and
 *       {@link SmtpEmailProvider} takes over.</li>
 *   <li><strong>@Value injection</strong> — the {@code fromAddress} is injected
 *       from application properties with a default value, showing how to
 *       externalise configuration.</li>
 *   <li><strong>Strategy + @Qualifier</strong> — the {@code emailTemplateEngine}
 *       is injected with {@code @Qualifier("emailTemplateEngine")} to
 *       distinguish it from the main web template engine (if any).</li>
 *   <li><strong>Thymeleaf template rendering</strong> — the provider renders
 *       the HTML content using a dedicated {@link org.thymeleaf.TemplateEngine}
 *       with a {@link org.thymeleaf.context.Context} populated with variables.</li>
 * </ul>
 * </p>
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
