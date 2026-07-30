package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for sending an email to users via the admin panel.
 *
 * <p>This record supports two modes of email delivery: targeted
 * (single recipient via {@code toEmail}) or broadcast (all users
 * via {@code sendToAll}). The compact constructor normalizes a
 * {@code null} {@code sendToAll} value to {@code false} to ensure
 * consistent behavior downstream. It is submitted to the admin
 * email management endpoint and validated before processing.</p>
 *
 * <p>The {@code emailType} field is optional and enables template
 * selection when the system supports multiple email templates
 * (e.g. promotional, informational, announcement). When omitted,
 * the service layer falls back to a default template.</p>
 *
 * @param toEmail   recipient email address; required when
 *                  {@code sendToAll} is {@code false}; validated
 *                  as a proper email format; max 255 characters
 * @param subject   email subject line, required, max 255 characters;
 *                  displayed in the recipient's inbox
 * @param body      email body content, required; plain text or HTML
 *                  depending on the configured template engine
 * @param emailType optional template identifier for selecting a
 *                  specific email layout; when null, the default
 *                  template is used
 * @param sendToAll whether to broadcast to all registered users;
 *                  defaults to {@code false} when null; when
 *                  {@code true}, the {@code toEmail} field is ignored
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminMailRequest(
        /**
         * Recipient email address. Required when {@code sendToAll} is false.
         */
        String toEmail,

        /**
         * Email subject. Must not be blank. Max 255 characters.
         */
        @NotBlank(message = "Subject is required.")
        @Size(max = 255, message = "Subject cannot exceed 255 characters.")
        String subject,

        /**
         * Email body content. Must not be blank.
         */
        @NotBlank(message = "Body is required.")
        String body,

        /**
         * Optional email type for template selection.
         */
        String emailType,

        /**
         * Whether to send to all users. Defaults to false if null.
         */
        Boolean sendToAll
) {
    /**
     * Compact constructor for cross-field validation.
     * Sets {@code sendToAll} to {@code false} when null.
     */
    public AdminMailRequest {
        if (sendToAll == null) sendToAll = false;
    }
}
