package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for requesting an email verification code.
 *
 * <p>This record is the first step in the email verification flow. When
 * a user needs to verify ownership of their email address, they submit
 * this DTO. The system sends a 6-digit verification code to the
 * provided email address, which the user then submits via
 * {@link ConfirmEmailRequest} to complete the verification process.</p>
 *
 * <p>The single-field design keeps the request minimal. The
 * {@code @NotBlank} and {@code @Email} annotations ensure that the
 * submitted value is non-empty and matches a valid email format. If
 * the email is not associated with any user account, the service layer
 * returns a generic success message to prevent email enumeration
 * attacks.</p>
 *
 * @param email the email address to send the verification code to;
 *              must be a valid email format and must correspond to an
 *              existing user account
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record VerifyEmailRequest(
        /**
         * Email address to send verification code to. Must be valid.
         */
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email
) {
}
