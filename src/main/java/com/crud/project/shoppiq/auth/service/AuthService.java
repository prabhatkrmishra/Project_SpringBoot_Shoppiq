package com.crud.project.shoppiq.auth.service;

import com.crud.project.shoppiq.auth.dto.JwtRequest;
import com.crud.project.shoppiq.auth.dto.JwtResponse;
import com.crud.project.shoppiq.auth.utils.JwtAuthenticationUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Service handling authentication logic and JWT cookie creation.
 * Orchestrates the login flow: credential validation, token generation, and
 * writing the token into an HttpOnly cookie on the HTTP response.
 *
 * <p>The JWT is never returned in a response body. Placing it in an HttpOnly
 * cookie means JavaScript cannot access it, which eliminates the most common
 * XSS-based token theft vector.</p>
 */
@Service
public class AuthService {

    /**
     * Long-lived token lifetime in ms — used when "Remember Me" is checked. Loaded from properties.
     */
    @Value("${jwt.expiration}")
    private long expirationTime;

    /**
     * Short-lived token lifetime in ms — used for a normal session. Loaded from properties.
     */
    @Value("${jwt.short-expiration}")
    private long shortExpiration;

    private final AuthenticationManager authManager;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final UserDetailsService userDetailsService;

    public AuthService(AuthenticationManager authManager,
                       JwtAuthenticationUtils jwtAuthenticationUtils,
                       UserDetailsService userDetailsService) {
        this.authManager = authManager;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Performs credential verification using Spring Security's {@link AuthenticationManager}.
     *
     * <p>Creates a {@link UsernamePasswordAuthenticationToken} and delegates to
     * {@code AuthenticationManager}, which internally uses {@link UserDetailsService}
     * to load the user from the database and {@code PasswordEncoder} to verify the
     * password. Roles and authorities are also resolved at this stage.</p>
     *
     * @param username user's login name
     * @param password plain-text password submitted by the client
     * @throws BadCredentialsException if authentication fails for any reason
     */
    private void authenticate(String username, String password) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password", ex);
        }
    }

    /**
     * Complete login workflow: validate credentials, generate a JWT, and deliver
     * it as an HttpOnly cookie on the response.
     *
     * <p>Cookie attributes applied:</p>
     * <ul>
     *   <li>{@code HttpOnly} — JavaScript cannot read the cookie, mitigating XSS.</li>
     *   <li>{@code Secure} — only transmitted over HTTPS.</li>
     *   <li>{@code SameSite=Strict} — not sent on cross-site requests, mitigating CSRF.</li>
     *   <li>{@code Path=/} — sent with every request to this server.</li>
     *   <li>{@code Max-Age} — set (in seconds) when {@code rememberMe} is {@code true},
     *       making the cookie persistent across browser restarts. Omitted for a normal
     *       session so the browser discards the cookie on close.</li>
     * </ul>
     *
     * @param request  {@link JwtRequest} containing username, password, and rememberMe flag
     * @param response servlet response to which the JWT cookie is attached
     * @return {@link JwtResponse} carrying a status message (token is in the cookie, not here)
     * @throws RuntimeException if any step of the login flow fails
     */
    public JwtResponse login(JwtRequest request, HttpServletResponse response) {
        try {
            // 1. Verify credentials via AuthenticationManager
            authenticate(request.getUsername(), request.getPassword());

            // 2. Resolve expiration: long-lived if rememberMe, short-lived otherwise
            boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
            long expiryMs = rememberMe ? expirationTime : shortExpiration;

            // 3. Generate signed JWT
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtAuthenticationUtils.generateToken(userDetails, expiryMs);

            // 4. Write token into an HttpOnly cookie — never into the response body
            int maxAgeSec = rememberMe ? (int) (expirationTime / 1000) : -1;
            response.addCookie(jwtAuthenticationUtils.buildJwtCookie(token, maxAgeSec));

            // 5. Return a plain status message; the token is inaccessible to JS by design
            return new JwtResponse("Login successful");
        } catch (Exception ex) {
            throw new RuntimeException("Login failed", ex);
        }
    }

    /**
     * Expires the JWT cookie by overwriting it with an empty value and {@code Max-Age=0}.
     *
     * <p>The browser deletes any cookie whose {@code Max-Age} is 0 immediately upon
     * receiving the response. This is the only way to clear an HttpOnly cookie —
     * JavaScript cannot delete it because it cannot read it.</p>
     *
     * @param response servlet response to which the expiring cookie is attached
     */
    public void logout(HttpServletResponse response) {
        response.addCookie(jwtAuthenticationUtils.buildJwtCookie("", 0));
    }
}