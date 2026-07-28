package com.pkmprojects.shoppiq.auth.dto;

/**
 * <strong>Spring Boot Concept:</strong> Data Transfer Object for the login response body.
 *
 * <h3>Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>JWT delivered as HttpOnly cookie (not response body)</strong> —
 *       unlike many tutorials that return the JWT in JSON and require the client
 *       to store it in {@code localStorage}, this application sets the JWT as
 *       an HttpOnly cookie. This prevents XSS-based token theft because
 *       JavaScript can never read the cookie value.</li>
 *   <li><strong>Minimal response surface</strong> — the response body contains
 *       only a status message; the actual credential travels in a
 *       {@code Set-Cookie} header, reducing the attack surface for token
 *       interception.</li>
 * </ul>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Java record</strong> — simple, immutable response wrapper.</li>
 *   <li><strong>Separation of credential from payload</strong> — the JWT is
 *       never serialized into the response body, keeping the authentication
 *       token out of JSON serialization/deserialization paths.</li>
 * </ul>
 *
 * <p>Example response body:</p>
 * <pre>
 * {
 *   "message": "Login successful"
 * }
 * </pre>
 *
 * @param message human-readable status message
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record JwtResponse(String message) {}
