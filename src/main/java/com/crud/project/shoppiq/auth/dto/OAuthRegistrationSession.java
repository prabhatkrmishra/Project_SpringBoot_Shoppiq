package com.crud.project.shoppiq.auth.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Immutable record holding the verified Google OAuth2 profile during
 * the registration completion window.
 *
 * <p>Stored in the HTTP session under {@code "oauth_user"} after successful
 * Google authentication for users who do not yet have a local account.
 * The session is invalidated once registration completes or expires.</p>
 *
 * <p>Using a Java {@code record} ensures immutability — the verified
 * Google data cannot be modified after being stored in the session.
 * The {@code authenticatedAt} timestamp allows the registration flow
 * to enforce a time limit on session validity.</p>
 *
 * <p>Flow path:</p>
 * <pre>
 * Google OAuth2 Login
 *       ↓
 * OAuth2SuccessHandler creates OAuthRegistrationSession
 *       ↓
 * Stored in HttpSession as "oauth_user"
 *       ↓
 * GET /auth/google/get-profile returns this to frontend
 *       ↓
 * POST /auth/google/complete-profile uses this to create User
 *       ↓
 * Session invalidated, JWT cookie issued
 * </pre>
 *
 * @param email           verified email address from Google's OIDC claims
 * @param name            full name from Google's OIDC claims
 * @param authenticatedAt timestamp when Google authentication completed
 */
public record OAuthRegistrationSession(
        String email,
        String name,
        Instant authenticatedAt
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}