package com.pkmprojects.shoppiq.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Immutable record holding the verified Google OAuth2 profile during
 * the registration completion window.
 *
 * <p>Stored in the {@code oauth2_registration} HttpOnly cookie after
 * successful Google authentication for users who do not yet have a local
 * account. The cookie is cleared once registration completes or the timeout
 * lapses.</p>
 *
 * <p>The {@code authenticatedAt} timestamp allows the registration endpoint
 * to enforce a time limit on cookie validity without any server-side state.</p>
 *
 * <p>Flow path:</p>
 * <pre>
 * Google OAuth2 Login
 *       ↓
 * OAuth2SuccessHandler creates OAuthRegistrationSession
 *       ↓
 * Stored in cookie via OAuthRegistrationCookieService
 *       ↓
 * GET /auth/google/get-profile returns this to frontend
 *       ↓
 * POST /auth/google/complete-profile uses this to create User
 *       ↓
 * Cookie cleared, JWT cookie issued
 * </pre>
 *
 * @param email           verified email address from Google's OIDC claims
 * @param name            full name from Google's OIDC claims
 * @param authenticatedAt timestamp when Google authentication completed
 */
public record OAuthRegistrationSession(
        @JsonProperty("email") String email,
        @JsonProperty("name")  String name,
        @JsonProperty("authenticatedAt") Instant authenticatedAt
) {
    /**
     * Jackson deserializer entry point.
     *
     * <p>Required because Java records do not have a no-arg constructor;
     * the {@code @JsonCreator} on the canonical constructor tells Jackson
     * how to build the record from JSON.</p>
     */
    @JsonCreator
    public OAuthRegistrationSession {
        // canonical constructor — validation could be added here
    }
}
