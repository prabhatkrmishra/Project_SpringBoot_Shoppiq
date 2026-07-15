package com.pkmprojects.shoppiq.auth.controller;

import com.pkmprojects.shoppiq.auth.dto.CompleteGoogleRegistrationRequest;
import com.pkmprojects.shoppiq.auth.dto.JwtRequest;
import com.pkmprojects.shoppiq.auth.dto.JwtResponse;
import com.pkmprojects.shoppiq.auth.dto.OAuthRegistrationSession;
import com.pkmprojects.shoppiq.auth.oauth2.OAuthRegistrationCookieService;
import com.pkmprojects.shoppiq.auth.service.AuthService;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.DuplicateUserException;
import com.pkmprojects.shoppiq.exception.auth.InvalidOidcUserException;
import com.pkmprojects.shoppiq.exception.auth.OAuthSessionException;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Handles login, logout, OAuth2 profile retrieval, and OAuth2 registration
 * completion. The JWT is delivered exclusively as an HttpOnly cookie via
 * {@link JwtCookieFactory} — it never appears in response bodies.</p>
 *
 * <h4>Fully stateless — no HttpSession</h4>
 * <p>OAuth2 registration state is held in the {@code oauth2_registration}
 * HttpOnly cookie managed by {@link OAuthRegistrationCookieService}. No
 * server-side session is created at any point in the authentication flow.</p>
 *
 *
 * <h4>Endpoint flow</h4>
 * <pre>
 * Google Login → OAuth2SuccessHandler stores OAuthRegistrationSession in cookie
 *       ↓
 * GET /auth/google/get-profile → reads cookie, returns name and email to frontend
 *       ↓
 * User chooses username + password → POST /auth/google/complete-profile
 *       ↓
 * Validate cookie exists and has not expired
 *       ↓
 * Create user via UserService.createGoogleUser()
 *       ↓
 * Generate JWT with userId, username, roles, tokenVersion
 *       ↓
 * Set HttpOnly JWT cookie
 *       ↓
 * Clear oauth2_registration cookie
 * </pre>
 *
 * @see OAuthRegistrationSession
 * @see OAuthRegistrationCookieService
 * @see CompleteGoogleRegistrationRequest
 * @see JwtCookieFactory
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final JwtCookieFactory jwtCookieFactory;
    private final OAuthRegistrationCookieService registrationCookieService;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.refresh-max-age:2592000000}")
    private long refreshMaxAge;

    @Value("${oauth.registration.timeout-minutes:10}")
    private int oauthRegistrationTimeoutMinutes;

    public AuthController(AuthService authService,
                          UserService userService,
                          UserRepository userRepository,
                          JwtAuthenticationUtils jwtAuthenticationUtils,
                          JwtCookieFactory jwtCookieFactory,
                          OAuthRegistrationCookieService registrationCookieService) {
        this.authService = authService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.jwtCookieFactory = jwtCookieFactory;
        this.registrationCookieService = registrationCookieService;
    }

    /**
     * Handles username/password login.
     * Delegates to {@link AuthService} for credential validation and JWT
     * cookie creation.
     *
     * @param jwtRequest contains username, password, and rememberMe flag
     * @param response   servlet response for setting the JWT cookie
     * @return 200 with status message
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest jwtRequest,
                                             HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(jwtRequest, response));
    }

    /**
     * Logs the user out by clearing the JWT cookie, any OAuth2 registration
     * cookie, and the Spring Security context.
     *
     * <p>Since the application is fully stateless, logout only needs to:</p>
     * <ol>
     *     <li>Expire the JWT cookie (Max-Age=0).</li>
     *     <li>Expire the OAuth2 registration cookie if present.</li>
     *     <li>Clear the SecurityContextHolder.</li>
     * </ol>
     *
     * @param response servlet response for clearing cookies
     * @return 200 with logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        authService.logout(response);
        registrationCookieService.clear(response);
        SecurityContextHolder.clearContext();
        logger.debug("User logged out successfully");
        return ResponseEntity.ok("Logout successful");
    }

    /**
     * Returns the OAuth2 registration session data to pre-populate the
     * registration completion form.
     *
     * <p>Reads the {@code oauth2_registration} cookie written by
     * {@link com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler}.</p>
     *
     * @param request incoming HTTP request (cookie source)
     * @return 200 with name and email
     * @throws OAuthSessionException if no OAuth registration cookie is present
     */
    @GetMapping("/google/get-profile")
    public ResponseEntity<OAuthRegistrationSession> getOauthProfile(HttpServletRequest request) {
        OAuthRegistrationSession oauthSession = registrationCookieService.read(request);
        if (oauthSession == null) {
            throw new OAuthSessionException(
                    "No OAuth registration session was found. Please sign in with Google again.");
        }
        return ResponseEntity.ok(oauthSession);
    }

    /**
     * Completes OAuth2 registration by creating a local account and issuing
     * a JWT cookie.
     *
     * <p>Reads the {@code oauth2_registration} cookie to retrieve the verified
     * Google profile. The username and password come from the request body;
     * email and name come from the cookie. On success the registration cookie
     * is cleared and a JWT cookie is written.</p>
     *
     * @param newRequest contains the chosen username and password
     * @param request    incoming HTTP request (cookie source)
     * @param response   servlet response for setting/clearing cookies
     * @return 201 with success message
     * @throws OAuthSessionException    if the cookie is missing or has expired
     * @throws InvalidOidcUserException if the session lacks a verified email
     * @throws DuplicateUserException   if the email or username is already taken
     */
    @PostMapping("/google/complete-profile")
    public ResponseEntity<String> completeProfile(
            @Valid @RequestBody CompleteGoogleRegistrationRequest newRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        OAuthRegistrationSession oauthSession = registrationCookieService.read(request);
        if (oauthSession == null) {
            throw new OAuthSessionException(
                    "No OAuth registration session was found. Please sign in with Google again.");
        }

        if (oauthSession.email() == null) {
            throw new InvalidOidcUserException("OIDC user does not contain a valid email claim.");
        }

        if (Instant.now().isAfter(oauthSession.authenticatedAt()
                .plus(oauthRegistrationTimeoutMinutes, ChronoUnit.MINUTES))) {
            registrationCookieService.clear(response);
            logger.debug("OAuth registration cookie expired for email: {}", oauthSession.email());
            throw new OAuthSessionException(
                    "OAuth registration session has expired. Please sign in with Google again.");
        }

        if (userRepository.findUserByEmail(oauthSession.email()).isPresent()) {
            throw DuplicateUserException.email(oauthSession.email());
        }

        if (userRepository.findUserByUsername(newRequest.username()).isPresent()) {
            throw DuplicateUserException.username(newRequest.username());
        }

        User user;
        try {
            user = userService.createGoogleUser(oauthSession, newRequest.username(), newRequest.password());
        } catch (DuplicateUserException e) {
            registrationCookieService.clear(response);
            throw e;
        }

        String token = jwtAuthenticationUtils.generateToken(user, expirationTime);
        response.addCookie(jwtCookieFactory.buildJwtCookie(token, (int) (expirationTime / 1000)));

        registrationCookieService.clear(response);
        logger.debug("Google OAuth2 registration completed for user: {}", newRequest.username());
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    /**
     * Refreshes the access token by validating the existing JWT cookie
     * (even if expired) against the user's current token version in the database.
     * If valid, issues a new JWT cookie with updated expiration.
     *
     * <p>This endpoint enables silent token refresh for the frontend without
     * requiring the user to re-enter credentials.</p>
     *
     * @param request  incoming HTTP request containing the JWT cookie
     * @param response servlet response for setting the new JWT cookie
     * @return 200 with success message, or 401 if refresh fails
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtAuthenticationUtils.extractJwtFromCookies(request);
        if (token == null) {
            logger.debug("Refresh failed: no JWT cookie present");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token to refresh");
        }

        Long userId;
        try {
            userId = jwtAuthenticationUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.debug("Refresh failed: invalid token structure");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !jwtAuthenticationUtils.validateTokenForRefresh(token, user, refreshMaxAge)) {
            logger.debug("Refresh failed: user not found or token validation failed for userId={}", userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token refresh failed");
        }

        String newToken = jwtAuthenticationUtils.generateToken(user, expirationTime);
        response.addCookie(jwtCookieFactory.buildJwtCookie(newToken, (int) (expirationTime / 1000)));

        logger.debug("Token refreshed successfully for user: {}", user.getUsername());
        return ResponseEntity.ok("Token refreshed");
    }

}
