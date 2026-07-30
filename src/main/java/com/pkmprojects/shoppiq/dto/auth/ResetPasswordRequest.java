package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for resetting a password with a verification code.
 *
 * <p>This record is the second step in the password reset flow. After
 * the user receives a verification code via the
 * {@link ForgotPasswordRequest} step, they submit this DTO with the
 * code and their desired new password. Upon successful validation,
 * the user's stored password hash is replaced with the new password
 * and all existing refresh tokens are invalidated for security.</p>
 *
 * <p>The {@code newPassword} field enforces password strength through
 * a regex pattern requiring at least one lowercase letter, one
 * uppercase letter, one digit, and one special character from the
 * set {@code @$!%*?&}. The minimum length is 8 characters. The
 * {@code code} must exactly match the 6-digit code sent to the
 * user's email address.</p>
 *
 * @param email       the email address associated with the account; must
 *                    match the address used in the forgot-password request
 * @param code        the 6-digit verification code received via email;
 *                    must be exactly 6 characters
 * @param newPassword the new password to set; must be at least 8
 *                    characters with uppercase, lowercase, digit,
 *                    and special character
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ResetPasswordRequest(
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
        String code,

        /**
         * New password. Must be at least 8 characters with uppercase, lowercase, number, and special character.
         */
        @NotBlank(message = "New password is required.")
        @Size(min = 8, message = "Password must be at least 8 characters.")
        @Pattern(
                regexp = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\p{N})(?=.*[@$!%*?&]).+$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String newPassword
) {
}
