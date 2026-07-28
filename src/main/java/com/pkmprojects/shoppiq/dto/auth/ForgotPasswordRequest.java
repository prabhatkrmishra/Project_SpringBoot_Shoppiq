package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * <strong>Spring Boot Concept:</strong> Request DTO for initiating a password reset.
 *
 * <p>A minimal Java record with a single validated field. This demonstrates
 * how simple request DTOs can be when the API only needs one piece of data
 * from the client.</p>
 *
 * <p><b>API flow:</b> Step 1 of password reset — validates the email exists
 * and sends a 6-digit code to that address.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email
) {}
