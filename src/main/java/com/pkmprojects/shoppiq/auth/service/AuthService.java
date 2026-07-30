package com.pkmprojects.shoppiq.auth.service;

import com.pkmprojects.shoppiq.auth.dto.JwtRequest;
import com.pkmprojects.shoppiq.auth.dto.JwtResponse;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.auth.InvalidCredentialException;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Handles authentication logic and JWT cookie creation.
 *
 * <p>This service orchestrates the complete username/password login flow,
 * including credential validation through the Spring Security
 * {@link org.springframework.security.authentication.AuthenticationManager},
 * account lockout after failed attempts, JWT token generation, and
 * stateless logout. It is the central component for email-based
 * authentication in the application.</p>
 *
 * <p>The service also manages the logout flow by clearing the JWT cookie
 * and invalidating the token version to force re-authentication. Account
 * lockout is implemented by tracking failed login attempts and locking
 * the account after a configurable threshold. The service uses
 * {@link com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils}
 * for token operations and {@link com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory}
 * for cookie management.</p>
 *
 * @author prabhatkrmishra
 * @see JwtAuthenticationUtils
 * @see JwtCookieFactory
 * @see CustomUserDetailService
 * @since 1.0.0
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final long expirationTime;
    private final long shortExpiration;
    private final AuthenticationManager authManager;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final UserRepository userRepository;
    private final JwtCookieFactory jwtCookieFactory;
    private final Clock clock;

    public AuthService(@Value("${jwt.expiration}") long expirationTime,
                       @Value("${jwt.short-expiration}") long shortExpiration,
                       AuthenticationManager authManager,
                       JwtAuthenticationUtils jwtAuthenticationUtils,
                       UserRepository userRepository,
                       JwtCookieFactory jwtCookieFactory,
                       Clock clock) {
        this.expirationTime = expirationTime;
        this.shortExpiration = shortExpiration;
        this.authManager = authManager;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.userRepository = userRepository;
        this.jwtCookieFactory = jwtCookieFactory;
        this.clock = clock;
    }

    /**
     * Validates credentials against Spring Security's authentication manager.
     *
     * @param username user's login name
     * @param password plain-text password from the client
     * @throws InvalidCredentialException if authentication fails
     */
    private void authenticate(String username, String password) {
        User user = userRepository.findUserByUsername(username).orElse(null);

        if (user != null && !user.isAccountNonLocked()) {
            throw new InvalidCredentialException("Account is locked. Please try again later.");
        }

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException _) {
            if (user != null) {
                // Runs in REQUIRES_NEW so the increment commits even when the
                // login ultimately fails (the caller's exception would roll back
                // a REQUIRED transaction).
                int updated = recordFailedLoginAttempt(user.getId());
                if (updated == 0) {
                    // Account is already locked out
                    throw new InvalidCredentialException("Account is locked. Please try again later.");
                }
            }
            throw new InvalidCredentialException("Invalid username or password");
        }

        if (user != null && user.getFailedLoginAttempts() > 0) {
            resetFailedLoginAttempts(user.getId());
        }
    }

    /**
     * Atomically increments the failed login counter and locks the account
     * when the threshold is reached.
     *
     * <p>Runs in a {@code REQUIRES_NEW} transaction so the increment is
     * committed even when the surrounding login ultimately fails — without
     * this, a thrown exception would roll back the counter.</p>
     *
     * @param userId the user whose attempts to record
     * @return rows updated (0 = already locked, 1 = incremented)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedLoginAttempt(Long userId) {
        return userRepository.incrementFailedLoginAttemptsAndLockout(userId, MAX_FAILED_ATTEMPTS, Instant.now(clock));
    }

    /**
     * Resets the failed login counter and lockout timestamp after a successful login.
     *
     * @param userId the user whose counter to reset
     */
    @Transactional
    public void resetFailedLoginAttempts(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setLockoutTime(null);
            userRepository.save(user);
        });
    }

    /**
     * Complete login workflow: validate credentials, generate a JWT with
     * all necessary claims for stateless authentication, and deliver it
     * as an HttpOnly cookie.
     *
     * @param request  contains username, password, and rememberMe flag
     * @param response servlet response to which the JWT cookie is attached
     * @return {@link JwtResponse} with status message
     * @throws InvalidCredentialException if credentials are invalid, or the
     *                                    authenticated user cannot be re-loaded
     */
    public JwtResponse login(JwtRequest request, HttpServletResponse response) {
        authenticate(request.username(), request.password());

        boolean rememberMe = Boolean.TRUE.equals(request.rememberMe());
        long expiryMs = rememberMe ? expirationTime : shortExpiration;

        User user = userRepository.findUserByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialException("Invalid username or password"));

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
