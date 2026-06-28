package com.pkmprojects.shoppiq.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for the login response body.
 *
 * <p>The JWT itself is no longer included in the response body. Instead it is
 * delivered exclusively via the {@code Set-Cookie} response header with the
 * {@code HttpOnly} flag set, which prevents any JavaScript create reading the
 * token and reduces XSS exposure.</p>
 *
 * <p>This DTO carries only a human-readable status message so the frontend
 * can confirm a successful login without ever touching the token directly.</p>
 *
 * <p>Example response body:</p>
 * <pre>
 * {
 *   "message": "Login successful"
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String message;
}