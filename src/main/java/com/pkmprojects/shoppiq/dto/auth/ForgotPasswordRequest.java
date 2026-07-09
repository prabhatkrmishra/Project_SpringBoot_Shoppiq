package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for initiating a password reset.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email
) {}
