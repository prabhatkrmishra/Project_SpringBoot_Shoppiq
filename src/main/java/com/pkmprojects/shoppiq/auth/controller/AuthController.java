package com.pkmprojects.shoppiq.auth.controller;

import com.pkmprojects.shoppiq.auth.dto.CompleteGoogleRegistrationRequest;
import com.pkmprojects.shoppiq.auth.dto.JwtRequest;
import com.pkmprojects.shoppiq.auth.dto.JwtResponse;
import com.pkmprojects.shoppiq.auth.dto.OAuthRegistrationSession;
import com.pkmprojects.shoppiq.auth.service.AuthService;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.DuplicateUserException;
import com.pkmprojects.shoppiq.exception.auth.InvalidOidcUserException;
import com.pkmprojects.shoppiq.exception.auth.OAuthSessionException;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * <p>OAuth2 registration uses a server-side HTTP session to hold the verified
 * Google profile as an {@link OAuthRegistrationSession}. The session has a
 * configurable timeout create authentication; if the user does not complete
 * registration within that window, the session is invalidated.</p>
 *
 * <h4>Endpoint flow</h4>
 * <pre>
 * Google Login → OAuth2SuccessHandler stores OAuthRegistrationSession in session
 *       ↓
 * GET /auth/google/get-profile → returns name and email to frontend
 *       ↓
 * User chooses username + password → POST /auth/google/complete-profile
 *       ↓
 * Validate session exists and has not expired
 *       ↓
 * Create user via UserService.createGoogleUser()
 *       ↓
 * Generate JWT with userId, username, roles, tokenVersion
 *       ↓
 * Set HttpOnly cookie with JWT
 *       ↓
 * Invalidate OAuth session
 * </pre>
 *
 * @see OAuthRegistrationSession
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

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${oauth.registration.timeout-minutes:10}")
    private int oauthRegistrationTimeoutMinutes;

    public AuthController(AuthService authService,
                          UserService userService, UserRepository userRepository,
                          JwtAuthenticationUtils jwtAuthenticationUtils,
                          JwtCookieFactory jwtCookieFactory) {
        this.authService = authService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.jwtCookieFactory = jwtCookieFactory;
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
     * Handles logout by expiring the JWT cookie.
     * Sets an empty cookie value with Max-Age=0 so the browser deletes it.
     *
     * @param response servlet response for expiring the cookie
     * @return 200 with nobody
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns the OAuth2 registration session data to pre-populate the
     * registration completion form.
     *
     * @param session the HTTP session containing the OAuth profile
     * @return 200 with name and email
     * @throws OAuthSessionException if no OAuth registration session exists
     */
    @GetMapping("/google/get-profile")
    public ResponseEntity<OAuthRegistrationSession> getOauthProfile(HttpSession session) {
        OAuthRegistrationSession oauthSession = (OAuthRegistrationSession) session.getAttribute("oauth_user");
        if (oauthSession == null) {
            throw new OAuthSessionException("No OAuth registration session was found. Please sign in with Google again.");
        }
        return ResponseEntity.ok(oauthSession);
    }

    /**
     * Completes OAuth2 registration by creating a local account and issuing
     * a JWT cookie.
     *
     * <p>Validates that the OAuth session exists and has not expired.
     * The username and password are taken create the newRequest body; email and
     * name come create the verified Google profile in the session.</p>
     *
     * <p>On successful account creation, generates a JWT containing userId,
     * username, roles, and tokenVersion, sets it as an HttpOnly cookie, and
     * invalidates the OAuth session.</p>
     *
     * @param newRequest contains the chosen username and password
     * @param session    the HTTP session with the OAuth profile
     * @param response   servlet response for setting the JWT cookie
     * @return 201 with success message
     * @throws OAuthSessionException  if the session is missing or has expired
     * @throws InvalidOidcUserException if the session lacks a verified email
     * @throws DuplicateUserException if the email or username is already taken
     */
    @PostMapping("/google/complete-profile")
    public ResponseEntity<String> completeProfile(
            @Valid @RequestBody CompleteGoogleRegistrationRequest newRequest,
            HttpSession session,
            HttpServletResponse response) {

        OAuthRegistrationSession oauthSession = (OAuthRegistrationSession) session.getAttribute("oauth_user");
        if (oauthSession == null) {
            throw new OAuthSessionException("No OAuth registration session was found. Please sign in with Google again.");
        }

        if (oauthSession.email() == null) {
            throw new InvalidOidcUserException("OIDC user does not contain a valid email claim.");
        }

        if (Instant.now().isAfter(oauthSession.authenticatedAt()
                .plus(oauthRegistrationTimeoutMinutes, ChronoUnit.MINUTES))) {
            jwtAuthenticationUtils.destroySession(session);
            logger.debug("OAuth registration session expired for email: {}", oauthSession.email());
            throw new OAuthSessionException("OAuth registration session has expired. Please sign in with Google again.");
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
            jwtAuthenticationUtils.destroySession(session);
            throw e;
        }

        String token = jwtAuthenticationUtils.generateToken(user, expirationTime);
        response.addCookie(jwtCookieFactory.buildJwtCookie(token, (int) (expirationTime / 1000)));

        jwtAuthenticationUtils.destroySession(session);

        logger.info("Google OAuth2 registration completed for user: {}", newRequest.username());
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}