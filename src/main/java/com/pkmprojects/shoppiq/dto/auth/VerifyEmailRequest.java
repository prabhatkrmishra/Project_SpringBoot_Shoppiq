package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for requesting email verification.
 *
 * <p>Like {@link ForgotPasswordRequest}, this is a single-field record using
 * {@code @NotBlank} + {@code @Email} validation. It follows the principle
 * of keeping DTOs as simple as possible while enforcing the API contract.</p>
 *
 * <p><b>API flow:</b> Step 1 of email verification — sends a 6-digit code
 * to the provided email address.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record VerifyEmailRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email
) {}
