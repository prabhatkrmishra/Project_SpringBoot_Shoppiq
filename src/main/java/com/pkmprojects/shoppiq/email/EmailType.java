package com.pkmprojects.shoppiq.email;

/**
 * <strong>Spring Boot Concept:</strong> Enum that enumerates all transactional
 * email types in the application. Each constant carries metadata
 * ({@code templateName}, {@code defaultSubject}) making it a self-describing
 * configuration object rather than a simple string constant.
 *
 * <p>
 * Each type maps to a specific Thymeleaf email template and a notification
 * preference flag in the {@link com.pkmprojects.shoppiq.entity.notification.NotificationPreference}
 * entity.
 * </p>
 *
 * <p><strong>Educational value:</strong> This enum demonstrates a pattern
 * where enum constants carry domain-relevant data:
 * <ul>
 *   <li><strong>Template routing</strong> — {@code getTemplateName()} returns
 *       the Thymeleaf template name, linking the enum to the view layer.</li>
 *   <li><strong>Default subject</strong> — each type has a fallback subject
 *       line used when no custom subject is provided.</li>
 *   <li><strong>Preference mapping</strong> — the service layer
 *       ({@link com.pkmprojects.shoppiq.email.impl.EmailServiceImpl}) maps
 *       each type to a notification preference flag to determine whether the
 *       email should be sent.</li>
 *   <li><strong>Domain-driven design</strong> — the enum makes the email
 *       domain explicit and type-safe, preventing invalid email types at
 *       compile time.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum EmailType {

    /**
     * Email verification for new accounts.
     */
    VERIFICATION("verification", "Verify your email address"),

    /**
     * Password reset security code.
     */
    PASSWORD_RESET("password-reset", "Reset your password"),

    /**
     * Security alerts (new login, password changed, etc.).
     */
    SECURITY_ALERT("security-alert", "Security alert"),

    /**
     * Order lifecycle updates (placed, shipped, delivered).
     */
    ORDER_UPDATE("order-update", "Order update"),

    /**
     * Welcome email after successful registration.
     */
    WELCOME("welcome", "Welcome to Shoppiq"),

    /**
     * Promotional emails (deals, discounts, offers).
     */
    PROMOTION("promotion", "Special offer for you"),

    /**
     * Review and engagement emails (review requests, community updates).
     */
    REVIEW_ENGAGEMENT("review-engagement", "We'd love your feedback"),

    /**
     * Admin-sent emails (custom messages from administrators).
     */
    ADMIN_MAIL("promotion", "Message from Shoppiq Admin");

    private final String templateName;
    private final String defaultSubject;

    EmailType(String templateName, String defaultSubject) {
        this.templateName = templateName;
        this.defaultSubject = defaultSubject;
    }

    /**
     * Returns the Thymeleaf template name (without .html extension).
     *
     * @return template name
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Returns the default subject line for this email type.
     *
     * @return default subject
     */
    public String getDefaultSubject() {
        return defaultSubject;
    }
}
