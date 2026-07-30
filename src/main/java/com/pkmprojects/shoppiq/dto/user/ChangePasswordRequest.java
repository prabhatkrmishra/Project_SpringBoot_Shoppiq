package com.pkmprojects.shoppiq.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for changing the current user's password.
 *
 * <p>This record is submitted to the password change endpoint when an
 * authenticated user wants to update their password. For credential-based
 * accounts, the {@code currentPassword} is required and must match the
 * stored password hash. For OAuth-only accounts (no stored password),
 * {@code currentPassword} is ignored and only the new password fields
 * are needed to set a password for the first time.</p>
 *
 * <p>This DTO uses Lombok annotations ({@code @Getter}, {@code @Setter},
 * {@code @NoArgsConstructor}, {@code @AllArgsConstructor}) instead of
 * a Java record because it has mutable fields and the
 * {@code currentPassword} field is conditionally required. The new
 * password enforces strength requirements through a regex pattern
 * requiring uppercase, lowercase, digit, and special character.</p>
 *
 * @param currentPassword the user's existing password; required for
 *                        credential-based accounts; ignored for
 *                        OAuth-only accounts; may be null for those accounts
 * @param newPassword     the new password to set; required; must be at least
 *                        8 characters with uppercase, lowercase, digit, and
 *                        special character from the set {@code @$!%*?&}
 * @param confirmPassword confirmation of the new password; must match
 *                        {@code newPassword}; validated at the service
 *                        layer for cross-field consistency
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChangePasswordRequest {

    /**
     * The user's existing password. Optional — only required for
     * credential-based accounts.
     */
    private String currentPassword;

    /**
     * New password. Must be at least 8 characters with uppercase, lowercase, number, and special character.
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\p{N})(?=.*[@$!%*?&]).+$",
            message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String newPassword;

    /**
     * Confirmation of the new password. Must match newPassword.
     */
    @NotBlank(message = "Please re-type the new password")
    private String confirmPassword;
}
