package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for submitting a contact message.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ContactMessageRequest(

        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must not exceed 100 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 255, message = "Email must not exceed 255 characters.")
        String email,

        @NotBlank(message = "Subject is required.")
        @Size(max = 200, message = "Subject must not exceed 200 characters.")
        String subject,

        @NotBlank(message = "Message is required.")
        @Size(max = 2000, message = "Message must not exceed 2000 characters.")
        String message
) {
}
