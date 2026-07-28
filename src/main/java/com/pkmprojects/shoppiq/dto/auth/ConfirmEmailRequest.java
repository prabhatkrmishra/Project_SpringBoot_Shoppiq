package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for confirming email verification with a code.
 *
 * <p>This Java record demonstrates <b>multi-field validation</b> using Jakarta
 * Bean Validation:</p>
 * <ul>
 *   <li>{@code @Email} — validates email format</li>
 *   <li>{@code @Size(min=6, max=6)} — exact-length constraint for a
 *       6-digit verification code</li>
 * </ul>
 *
 * <p><b>API flow:</b> Step 2 of email verification — the user submits the code
 * they received after calling {@link VerifyEmailRequest}.</p>
 *
 * @author prabhatkrmishra
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
