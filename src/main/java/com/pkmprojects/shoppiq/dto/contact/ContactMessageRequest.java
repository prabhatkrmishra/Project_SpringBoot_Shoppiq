package com.pkmprojects.shoppiq.dto.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for submitting a contact message through the support form.
 *
 * <p>This record is submitted to {@code POST /api/contact} when a
 * customer or visitor wants to send a message to the platform's support
 * team. All fields are required and validated using Jakarta Bean
 * Validation annotations before the request reaches the service layer.
 * The message is persisted with an initial status of OPEN for
 * subsequent admin triage.</p>
 *
 * <p>The {@code email} field is used for reply correspondence and must
 * be a valid email address. The {@code subject} helps categorize the
 * message for routing purposes. The {@code message} body supports
 * up to 2000 characters to accommodate detailed inquiries.</p>
 *
 * @param name    sender's display name, required, max 100 characters;
 *                used for personalizing admin-side message display
 * @param email   sender's email address, required, max 255 characters;
 *                must be a valid email format; used for reply correspondence
 * @param subject message subject line, required, max 200 characters;
 *                used for message categorization and routing
 * @param message message body text, required, max 2000 characters;
 *                contains the customer's inquiry or feedback
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ContactMessageRequest(

        /**
         * Sender's name. Must not be blank. Max 100 characters.
         */
        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must not exceed 100 characters.")
        String name,

        /**
         * Sender's email address. Must be valid. Max 255 characters.
         */
        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 255, message = "Email must not exceed 255 characters.")
        String email,

        /**
         * Message subject. Must not be blank. Max 200 characters.
         */
        @NotBlank(message = "Subject is required.")
        @Size(max = 200, message = "Subject must not exceed 200 characters.")
        String subject,

        /**
         * Message body. Must not be blank. Max 2000 characters.
         */
        @NotBlank(message = "Message is required.")
        @Size(max = 2000, message = "Message must not exceed 2000 characters.")
        String message
) {
}
