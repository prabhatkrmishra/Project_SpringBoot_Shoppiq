package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for confirming email verification with a code.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ConfirmEmailRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email,

        @NotBlank(message = "Verification code is required.")
        @Size(min = 6, max = 6, message = "Verification code must be 6 digits.")
        String code
) {}
