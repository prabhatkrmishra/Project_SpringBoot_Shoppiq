package com.crud.project.shoppiq.auth.service;

import com.crud.project.shoppiq.auth.dto.JwtRequest;
import com.crud.project.shoppiq.auth.dto.JwtResponse;
import com.crud.project.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.crud.project.shoppiq.auth.utils.JwtCookieFactory;
import com.crud.project.shoppiq.models.User;
import com.crud.project.shoppiq.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Service handling authentication logic and JWT cookie creation.
 *
 * <p>Orchestrates the complete login flow: credential validation via
 * {@link AuthenticationManager}, user lookup, token generation via
 * {@link JwtAuthenticationUtils}, and cookie creation via
 * {@link JwtCookieFactory}. The JWT is delivered exclusively as an
 * HttpOnly cookie, never in a response body.</p>
 *
 * <h4>Login flow</h4>
 * <pre>
 * POST /auth/login with username + password + rememberMe
 *       ↓
 * AuthService.login()
 *       ↓
 * authenticate() → AuthenticationManager validates credentials
 *       ↓
 * Load User from database
 *       ↓
 * JwtAuthenticationUtils.generateToken(user, expiryMs)
 *       ↓
 * Token contains: userId, username, roles, tokenVersion
 *       ↓
 * JwtCookieFactory.buildJwtCookie() → HttpOnly cookie
 *       ↓
 * Cookie added to HttpServletResponse
 * </pre>
 *
 * <h4>Logout flow</h4>
 * <pre>
 * POST /auth/logout
 *       ↓
 * AuthService.logout()
 *       ↓
 * Build cookie with empty value and Max-Age=0
 *       ↓
 * Browser deletes the JWT cookie immediately
 * </pre>
 *
 * @see JwtAuthenticationUtils
 * @see JwtCookieFactory
 */
@Service
public class AuthService {

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.short-expiration}")
    private long shortExpiration;

    private final AuthenticationManager authManager;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final UserRepository userRepository;
    private final JwtCookieFactory jwtCookieFactory;

    public AuthService(AuthenticationManager authManager,
                       JwtAuthenticationUtils jwtAuthenticationUtils,
                       UserRepository userRepository,
                       JwtCookieFactory jwtCookieFactory) {
        this.authManager = authManager;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.userRepository = userRepository;
        this.jwtCookieFactory = jwtCookieFactory;
    }

    /**
     * Validates credentials against Spring Security's authentication manager.
     *
     * @param username user's login name
     * @param password plain-text password from the client
     * @throws BadCredentialsException if authentication fails
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
     * Complete login workflow: validate credentials, generate a JWT with
     * all necessary claims for stateless authentication, and deliver it
     * as an HttpOnly cookie.
     *
     * @param request  contains username, password, and rememberMe flag
     * @param response servlet response to which the JWT cookie is attached
     * @return {@link JwtResponse} with status message
     * @throws BadCredentialsException if credentials are invalid
     */
    public JwtResponse login(JwtRequest request, HttpServletResponse response) {
        authenticate(request.getUsername(), request.getPassword());

        boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
        long expiryMs = rememberMe ? expirationTime : shortExpiration;

        User user = userRepository.findUserByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String token = jwtAuthenticationUtils.generateToken(user, expiryMs);

        int maxAgeSec = rememberMe ? (int) (expirationTime / 1000) : -1;
        response.addCookie(jwtCookieFactory.buildJwtCookie(token, maxAgeSec));

        return new JwtResponse("Login successful");
    }

    /**
     * Expires the JWT cookie by setting an empty value with Max-Age=0.
     * The browser deletes the cookie immediately upon receiving this response.
     *
     * @param response servlet response to which the expiring cookie is attached
     */
    public void logout(HttpServletResponse response) {
        response.addCookie(jwtCookieFactory.buildJwtCookie("", 0));
    }
}