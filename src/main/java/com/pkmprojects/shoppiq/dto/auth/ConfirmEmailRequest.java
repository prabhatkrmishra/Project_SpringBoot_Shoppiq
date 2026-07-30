package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for confirming email verification with a 6-digit code.
 *
 * <p>This record is the second step in the email verification flow. After
 * the user requests a verification code via
 * {@link VerifyEmailRequest}, they submit this DTO with the received
 * code to confirm ownership of the email address. Upon successful
 * validation, the user's {@code emailVerified} flag is set to
 * {@code true} in the database.</p>
 *
 * <p>Both fields are required: the {@code email} identifies which
 * account to verify, and the {@code code} must exactly match the
 * 6-digit code sent to that email address. The code expires after a
 * configured time window and can only be used once.</p>
 *
 * @param email the email address to verify; must be a valid email
 *              format and must match an existing user account
 * @param code  the 6-digit verification code received via email;
 *              must be exactly 6 characters; case-sensitive matching
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ConfirmEmailRequest(
        /**
         * Email address. Must be valid.
         */
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email,

        /**
         * 6-digit verification code. Must be exactly 6 characters.
         */
        @NotBlank(message = "Verification code is required.")
        @Size(min = 6, max = 6, message = "Verification code must be 6 digits.")
        String code
) {
}
