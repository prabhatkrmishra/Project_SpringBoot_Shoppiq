package com.pkmprojects.shoppiq.auth.service;

import com.pkmprojects.shoppiq.auth.dto.JwtRequest;
import com.pkmprojects.shoppiq.auth.dto.JwtResponse;
import java.time.Clock;
import java.time.Instant;
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

/**
 * Service handling authentication logic and JWT cookie creation — the
 * orchestration layer for the username/password login flow.
 *
 * <h3>Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>AuthenticationManager</strong> — the central authentication
 *       strategy. The service creates a {@link UsernamePasswordAuthenticationToken}
 *       and passes it to {@code AuthenticationManager.authenticate()}, which
 *       delegates to {@code DaoAuthenticationProvider} → {@link CustomUserDetailService}
 *       → {@code PasswordEncoder} for credential verification.</li>
 *   <li><strong>Account lockout pattern</strong> — tracks
 *       {@code failedLoginAttempts} and locks the account after 5 failed
 *       attempts by setting {@code lockoutTime}. This is a brute-force
 *       protection mechanism implemented at the application layer.</li>
 *   <li><strong>Remember-me via cookie Max-Age</strong> — instead of Spring
 *       Security's {@code RememberMeServices} (which stores a persistent token),
 *       this uses the JWT cookie's {@code Max-Age}: {@code -1} for session
 *       cookie (expires on browser close) or a positive value for persistent
 *       cookie (survives browser restart).</li>
 *   <li><strong>Stateless logout</strong> — since there is no server-side
 *       session, logout simply clears the JWT cookie. There is no session
 *       to invalidate, making logout an idempotent, stateless operation.</li>
 * </ul>
 *
 * <h3>Login flow</h3>
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
 * <h3>Logout flow</h3>
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
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Service layer pattern</strong> — encapsulates the complete login
 *       workflow (authentication + token generation + cookie creation) behind a
 *       single {@code login()} method called by the controller.</li>
 *   <li><strong>Defense in depth</strong> — the service checks account lockout
 *       <em>before</em> calling {@code AuthenticationManager.authenticate()},
 *       providing an early rejection path that avoids unnecessary password hashing.</li>
 *   <li><strong>Idempotent logout</strong> — clearing a cookie with {@code Max-Age=0}
 *       is idempotent; calling {@code logout()} multiple times has no additional
 *       effect.</li>
 * </ul>
 *
 * @see JwtAuthenticationUtils
 * @see JwtCookieFactory
 * @see CustomUserDetailService
 *
 * @author prabhatkrmishra
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
     *                                     authenticated user cannot be re-loaded
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
