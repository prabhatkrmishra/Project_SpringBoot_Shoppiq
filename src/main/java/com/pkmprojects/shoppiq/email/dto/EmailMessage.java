package com.pkmprojects.shoppiq.email.dto;

import com.pkmprojects.shoppiq.email.EmailType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * <strong>Spring Boot Concept:</strong> Immutable Data Transfer Object (DTO)
 * representing an email message to be sent via the {@link com.pkmprojects.shoppiq.email.EmailService}.
 *
 * <p>Uses Lombok's {@code @Getter} and {@code @Builder} annotations to
 * eliminate boilerplate — a common pattern in Spring Boot applications
 * for request/response DTOs. The builder pattern makes construction
 * readable when there are many optional fields.</p>
 *
 * <p><strong>Educational value:</strong> DTOs are the data carriers that
 * flow between layers in a layered architecture. This DTO carries the
 * information needed by the service layer (see {@link com.pkmprojects.shoppiq.email.impl.EmailServiceImpl})
 * and the provider layer (see {@link com.pkmprojects.shoppiq.email.provider.EmailProvider}).
 * Key design decisions:
 * <ul>
 *   <li><strong>Template-based rendering</strong> — the email body is not
 *       stored in the DTO; instead, a {@code templateName} + {@code variables}
 *       map is passed, and the provider renders it via Thymeleaf.</li>
 *   <li><strong>EmailType for routing</strong> — the {@link com.pkmprojects.shoppiq.email.EmailType}
 *       enum guides both template selection and notification preference checking.</li>
 *   <li><strong>Nullable userId</strong> — allows system-originated emails
 *       (e.g. admin broadcasts) where no specific user is associated.</li>
 * </ul>
 * </p>
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
