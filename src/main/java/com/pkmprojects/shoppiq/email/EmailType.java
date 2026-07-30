package com.pkmprojects.shoppiq.email;

/**
 * Enumerates all transactional email types with template routing and default subjects.
 *
 * <p>Each type maps to a Thymeleaf template and a notification preference flag for delivery control.
 * The email type determines which template is used to render the email content, what the default
 * subject line is, and which notification preference setting controls whether the email is sent.
 * This centralized routing ensures consistent email formatting and delivery behavior across the
 * application.</p>
 *
 * <p>The email type is also used for logging and auditing purposes, allowing administrators to
 * track email delivery statistics by type. Each type has a unique template name that maps to
 * a Thymeleaf HTML template file in the email templates directory.</p>
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

    /**
     * The Thymeleaf template name (without .html extension).
     */
    private final String templateName;

    /**
     * The default subject line for this email type.
     */
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
