package com.pkmprojects.shoppiq.email.dto;

import com.pkmprojects.shoppiq.email.EmailType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Immutable DTO representing an email message to be sent via the EmailService.
 *
 * <p>Carries template-based rendering data with EmailType for routing and preference checking.
 * This DTO encapsulates all the information needed to send an email: recipient address, subject,
 * template name, email type for preference checking, template variables for dynamic content,
 * and an optional user ID for logging purposes. The DTO is immutable and uses the builder
 * pattern for construction.</p>
 *
 * <p>The email type is used to determine whether the email should be sent based on user
 * notification preferences. Template variables are passed to the Thymeleaf template engine
 * for rendering the email content. The user ID is optional and used for audit logging when
 * available.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Getter
@Builder
public class EmailMessage {

    /**
     * Recipient email address.
     */
    private String to;

    /**
     * Email subject line.
     */
    private String subject;

    /**
     * Thymeleaf template name (without .html extension).
     */
    private String templateName;

    /**
     * Type of email for logging and preference checking.
     */
    private EmailType emailType;

    /**
     * Template variables to pass to Thymeleaf.
     */
    private Map<String, Object> variables;

    /**
     * User ID for logging purposes (nullable).
     */
    private Long userId;
}
