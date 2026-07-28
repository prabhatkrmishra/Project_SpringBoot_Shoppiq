package com.pkmprojects.shoppiq.auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.StringJoiner;

/**
 * Data Transfer Object for the login request payload.
 *
 * <h3>Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>Remember-me pattern</strong> — the {@code rememberMe} flag
 *       controls whether the JWT cookie is a session cookie ({@code Max-Age=-1},
 *       discarded on browser close) or a persistent cookie ({@code Max-Age}
 *       set to the JWT expiration). This is a stateless alternative to Spring
 *       Security's {@code RememberMeServices} which typically uses a persistent
 *       token approach.</li>
 *   <li><strong>Credential transport</strong> — the username and password arrive
 *       over HTTPS in the request body, then are validated by {@code AuthenticationManager}
 *       via {@code UsernamePasswordAuthenticationToken} in {@link com.pkmprojects.shoppiq.auth.service.AuthService}.</li>
 * </ul>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Java record</strong> — concise, immutable DTO with auto-generated
 *       accessors, {@code equals()}, and {@code hashCode()}.</li>
 *   <li><strong>Bean Validation</strong> — {@code @NotBlank} ensures mandatory fields
 *       are validated at the controller boundary.</li>
 * </ul>
 *
 * @param username   the user's login name
 * @param password   the user's password
 * @param rememberMe whether the session should persist across browser restarts
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record JwtRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password,
        Boolean rememberMe
) {
    /**
     * Custom {@code toString()} that excludes the password field
     * to prevent credentials from appearing in logs (BUG-0012).
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", JwtRequest.class.getSimpleName() + "[", "]")
                .add("username='" + username + "'")
                .add("rememberMe=" + rememberMe)
                .toString();
    }
}
