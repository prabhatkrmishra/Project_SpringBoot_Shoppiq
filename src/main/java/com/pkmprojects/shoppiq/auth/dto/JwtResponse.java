package com.pkmprojects.shoppiq.auth.dto;

/**
 * Data Transfer Object for the login response body.
 *
 * <p>Unlike many tutorials that return the JWT in JSON and require the client
 * to store it in {@code localStorage}, this application sets the JWT as an
 * HttpOnly cookie. This prevents XSS-based token theft because JavaScript can
 * never read the cookie value. The response body contains only a status message;
 * the actual credential travels in a {@code Set-Cookie} header, reducing the
 * attack surface for token interception.</p>
 *
 * <p>Example response body:</p>
 * <pre>
 * {
 *   "message": "Login successful"
 * }
 * </pre>
 *
 * @param message human-readable status message
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record JwtResponse(String message) {
}
