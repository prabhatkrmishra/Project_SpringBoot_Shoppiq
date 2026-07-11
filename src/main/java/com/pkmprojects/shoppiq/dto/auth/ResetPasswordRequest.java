package com.pkmprojects.shoppiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for resetting a password with a verification code.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email,

        @NotBlank(message = "Verification code is required.")
        @Size(min = 6, max = 6, message = "Verification code must be 6 digits.")
        String code,

        @NotBlank(message = "New password is required.")
        @Size(min = 8, message = "Password must be at least 8 characters.")
        @Pattern(
                regexp = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\p{N})(?=.*[@$!%*?&]).+$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String newPassword
) {
}
