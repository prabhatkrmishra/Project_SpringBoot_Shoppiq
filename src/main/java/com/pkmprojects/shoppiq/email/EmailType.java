package com.pkmprojects.shoppiq.email;

/**
 * Enumerates the types of transactional emails supported by Shoppiq.
 *
 * <p>
 * Each type maps to a specific email template and notification preference flag.
 * </p>
 *
 * @author PrabhatKrMishra
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
