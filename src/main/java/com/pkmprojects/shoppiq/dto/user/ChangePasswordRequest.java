package com.pkmprojects.shoppiq.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for changing the current user's password.
 *
 * <p>
 * For credential-based accounts {@code currentPassword} is required and must
 * match the stored password. For OAuth-only accounts (no stored password)
 * {@code currentPassword} is ignored and only {@code newPassword} together
 * with {@code confirmPassword} are needed to set a password.
 * </p>
 *
 * @author PrabhatKrMishra
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

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be at least 8 characters")
    private String newPassword;

    @NotBlank(message = "Please re-type the new password")
    private String confirmPassword;
}
