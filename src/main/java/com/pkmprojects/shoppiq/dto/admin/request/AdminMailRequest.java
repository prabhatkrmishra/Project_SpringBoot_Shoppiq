package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for admin sending an email to a user or all users.
 *
 * @author PrabhatKrMishra
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
