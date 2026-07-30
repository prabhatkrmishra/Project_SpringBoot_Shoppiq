package com.pkmprojects.shoppiq.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Immutable record holding the verified Google OAuth2 profile during
 * the registration completion window.
 *
 * <p>This record captures {@code email} and {@code name} claims from Google's
 * OpenID Connect identity token, bridging the OAuth2 callback and the local
 * registration form. The {@code authenticatedAt} timestamp enables the
 * registration endpoint to enforce a time limit
 * ({@code oauth.registration.timeout-minutes}) without any server-side session
 * storage. If the cookie is replayed after the timeout, registration is
 * rejected.</p>
 *
 * <p>The email and name originate from Google's signed OIDC token, are
 * serialized into an HMAC-signed cookie, and the client never supplies identity
 * data. This prevents a malicious client from registering with a forged email.
 * The flow proceeds from Google OAuth2 Login through OAuth2SuccessHandler
 * creating this session, storage in a cookie via OAuthRegistrationCookieService,
 * frontend retrieval via GET /auth/google/get-profile, and completion via
 * POST /auth/google/complete-profile which creates the User.</p>
 *
 * @param email           verified email address from Google's OIDC claims
 * @param name            full name from Google's OIDC claims
 * @param authenticatedAt timestamp when Google authentication completed
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record OAuthRegistrationSession(
        @JsonProperty("email") String email,
        @JsonProperty("name") String name,
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
