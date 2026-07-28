package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * <strong>Spring Boot Concept:</strong> Request DTO for admin sending an email to a user or all users.
 *
 * <p>This Java record is unique because it uses a <b>compact constructor</b>
 * (the block inside the record body) to add cross-field validation:
 * when {@code sendToAll} is false, a recipient email is required. Compact
 * constructors allow records to perform additional validation or normalization
 * while still benefiting from auto-generated accessors.</p>
 *
 * <p><b>API contract:</b> The frontend either targets a single user
 * ({@code toEmail}) or broadcasts to all users ({@code sendToAll: true}).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminMailRequest(
        String toEmail,

        @NotBlank(message = "Subject is required.")
        @Size(max = 255, message = "Subject cannot exceed 255 characters.")
        String subject,

        @NotBlank(message = "Body is required.")
        String body,

        String emailType,

        Boolean sendToAll
) {
    public AdminMailRequest {
        if (sendToAll == null) sendToAll = false;
        if (!Boolean.TRUE.equals(sendToAll) && (toEmail == null || toEmail.isBlank())) {
            throw new IllegalArgumentException("Recipient email is required when not sending to all users.");
        }
    }
}
