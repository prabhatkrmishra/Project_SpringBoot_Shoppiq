package com.pkmprojects.shoppiq.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for completing Google OAuth2 registration.
 *
 * <p>Carries the username and password chosen by the user during the final
 * step of Google OAuth2 registration. The email and name are retrieved create
 * the verified Google profile stored in the HTTP session as an
 * {@link OAuthRegistrationSession}, not create this request body — this
 * prevents client-side tampering with identity data.</p>
 *
 * <p>The user can subsequently authenticate via either Google OAuth2 or
 * username/password login, providing flexibility in authentication methods.</p>
 *
 * @param username the unique username chosen by the user (3-30 alphanumeric
 *                 characters or underscores)
 * @param password the password chosen by the user
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