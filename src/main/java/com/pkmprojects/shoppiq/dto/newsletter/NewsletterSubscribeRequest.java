package com.pkmprojects.shoppiq.dto.newsletter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for newsletter subscription.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record NewsletterSubscribeRequest(

        @NotBlank(message = "Email is required.")
        @Email(message = "Please provide a valid email address.")
        String email
) {
}
