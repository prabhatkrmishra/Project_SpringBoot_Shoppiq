package com.pkmprojects.shoppiq.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Immutable record holding the verified Google OAuth2 profile during
 * the registration completion window.
 *
 * <h3>Spring Security / OAuth2 concepts demonstrated</h3>
 * <ul>
 *   <li><strong>OIDC claims transport</strong> — this record captures
 *       {@code email} and {@code name} claims from Google's OpenID Connect
 *       identity token, bridging the OAuth2 callback and the local registration
 *       form.</li>
 *   <li><strong>Server-side timeout enforcement</strong> — the
 *       {@code authenticatedAt} timestamp enables the registration endpoint
 *       to enforce a time limit ({@code oauth.registration.timeout-minutes})
 *       without any server-side session storage. If the cookie is replayed
 *       after the timeout, registration is rejected.</li>
 *   <li><strong>Tamper-proof identity chain</strong> — the email and name
 *       originate from Google's signed OIDC token, are serialized into an
 *       HMAC-signed cookie, and the client never supplies identity data.
 *       This prevents a malicious client from registering with a forged email.</li>
 * </ul>
 *
 * <h3>Flow path</h3>
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
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Java record</strong> — immutable, compact, and JSON-serializable
 *       via {@link com.fasterxml.jackson.annotation.JsonCreator}.</li>
 *   <li><strong>Single-responsibility session DTO</strong> — the record carries
 *       only the identity data needed for registration, nothing more.</li>
 * </ul>
 *
 * @param email           verified email address from Google's OIDC claims
 * @param name            full name from Google's OIDC claims
 * @param authenticatedAt timestamp when Google authentication completed
 *
 * @author prabhatkrmishra
 * @since 1.0.0
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
