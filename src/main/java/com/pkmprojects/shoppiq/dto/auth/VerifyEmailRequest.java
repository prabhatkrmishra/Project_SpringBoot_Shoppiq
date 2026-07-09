package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for requesting email verification.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record VerifyEmailRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email
) {}
