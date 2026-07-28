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
 * <p>
 * For credential-based accounts {@code currentPassword} is required and must
 * match the stored password. For OAuth-only accounts (no stored password)
 * {@code currentPassword} is ignored and only {@code newPassword} together
 * with {@code confirmPassword} are needed to set a password.
 * </p>
 *
 * <p><b>Lombok vs Record:</b> This DTO uses <b>Lombok</b> annotations
 * ({@code @Getter}, {@code @Setter}, {@code @NoArgsConstructor},
 * {@code @AllArgsConstructor}) instead of Java records because it has
 * mutable fields ({@code currentPassword} is optional) and the class
 * may need to be constructed piecemeal. This demonstrates that records
 * are not always the right choice — Lombok offers flexibility when
 * mutable state or partial construction is needed.</p>
 *
 * <p><b>Password strength validation:</b> Uses the same {@code @Pattern}
 * regex as other password fields in the project, ensuring consistent
 * password requirements across all DTOs.</p>
 *
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

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\p{N})(?=.*[@$!%*?&]).+$",
            message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String newPassword;

    @NotBlank(message = "Please re-type the new password")
    private String confirmPassword;
}
