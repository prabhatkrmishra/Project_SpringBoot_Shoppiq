package com.pkmprojects.shoppiq.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for completing Google OAuth2 registration.
 *
 * <p>The email and name fields come exclusively from the verified Google OIDC
 * claims stored in the {@link OAuthRegistrationSession} cookie. The client
 * only supplies username and password, preventing tampering with identity data.
 * After completing this flow, the user can authenticate via either Google
 * OAuth2 or username/password, demonstrating multifactor identity linking.</p>
 *
 * <p>This record uses Bean Validation via {@code jakarta.validation} to enforce
 * username and password rules at the controller boundary before any business
 * logic runs. The username must be 3-30 alphanumeric characters or underscores.
 * The password requires at least one lowercase, uppercase, digit, and special
 * character via a regex pattern.</p>
 *
 * @param username the unique username chosen by the user (3-30 alphanumeric
 *                 characters or underscores)
 * @param password the password chosen by the user
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CompleteGoogleRegistrationRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
                regexp = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\p{N})(?=.*[@$!%*?&]).+$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String password

) {
}
