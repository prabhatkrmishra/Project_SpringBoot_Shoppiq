package com.pkmprojects.shoppiq.auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.StringJoiner;

/**
 * Data Transfer Object for the login request payload.
 *
 * <p>The {@code rememberMe} flag controls whether the JWT cookie is a session
 * cookie ({@code Max-Age=-1}, discarded on browser close) or a persistent
 * cookie ({@code Max-Age} set to the JWT expiration). This is a stateless
 * alternative to Spring Security's {@code RememberMeServices} which typically
 * uses a persistent token approach. The username and password arrive over HTTPS
 * in the request body, then are validated by {@code AuthenticationManager} via
 * {@code UsernamePasswordAuthenticationToken} in
 * {@link com.pkmprojects.shoppiq.auth.service.AuthService}.</p>
 *
 * <p>This record uses Bean Validation with {@code @NotBlank} to ensure
 * mandatory fields are validated at the controller boundary. The immutable
 * Java record provides auto-generated accessors, {@code equals()}, and
 * {@code hashCode()}.</p>
 *
 * @param username   the user's login name
 * @param password   the user's password
 * @param rememberMe whether the session should persist across browser restarts
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
