package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for initiating a password reset flow.
 *
 * <p>This record is the first step in the password reset flow. When a
 * user forgets their password, they submit their email address via
 * this DTO. The system validates that the email exists in the database
 * and sends a 6-digit verification code to that address. The code is
 * then used in the subsequent {@link ResetPasswordRequest} step to
 * complete the password reset.</p>
 *
 * <p>The single-field design demonstrates how minimal a request DTO can
 * be when the API only needs one piece of data from the client. The
 * {@code @NotBlank} and {@code @Email} annotations ensure that the
 * submitted value is non-empty and matches a valid email format before
 * reaching the service layer.</p>
 *
 * @param email the email address associated with the user's account;
 *              must be a valid email format and must correspond to an
 *              existing user account; the verification code is sent
 *              to this address
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ForgotPasswordRequest(
        /**
         * Email address to send password reset code to. Must be valid.
         */
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email
) {
}
