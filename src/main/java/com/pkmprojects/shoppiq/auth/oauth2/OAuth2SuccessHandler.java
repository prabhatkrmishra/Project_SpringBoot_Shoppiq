package com.pkmprojects.shoppiq.auth.oauth2;

import com.pkmprojects.shoppiq.auth.dto.OAuthRegistrationSession;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.auth.InvalidOidcUserException;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Handles successful Google OAuth2 authentication — Spring Security's
 * {@link AuthenticationSuccessHandler} for the OAuth2 login flow.
 *
 * <h3>OAuth2 / Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>AuthenticationSuccessHandler contract</strong> — Spring Security
 *       calls this after the OAuth2 authorization code exchange and token
 *       validation succeed. It replaces the default redirect behavior with
 *       custom post-login logic.</li>
 *   <li><strong>OIDC principal validation</strong> — verifies the principal
 *       is an {@link org.springframework.security.oauth2.core.oidc.user.OidcUser}
 *       and checks Google's {@code email_verified} claim before proceeding.
 *       This prevents account takeover using unverified Google accounts.</li>
 *   <li><strong>Returning vs. new user branching</strong> — existing users
 *       receive a JWT cookie and redirect to the original page; new users
 *       are diverted to a registration-completion flow, with their verified
 *       Google profile stored in an HttpOnly cookie.</li>
 *   <li><strong>Stateless OAuth2 integration</strong> — the handler writes
 *       temporary session state (for new users) into a cookie rather than
 *       the HTTP session, maintaining full statelessness.</li>
 *   <li><strong>Return-URL preservation</strong> — reads the
 *       {@code oauth_return_url} cookie (set by {@link OAuthReturnUrlFilter})
 *       so users are redirected back to the page they were on when they
 *       clicked "Login with Google".</li>
 * </ul>
 *
 * <h3>Branching flow</h3>
 * <pre>
 * Google authenticates user → onAuthenticationSuccess() called
 *       ↓
 * Verify principal is OidcUser (type check, not just cast)
 *       ↓
 * Verify email_verified claim is true
 *       ↓
 * Look up email in local database
 *       ↓
 * ┌─ Existing user → generate JWT cookie
 * │                  → read oauth_return_url cookie
 * │                  → redirect to return URL (or /allitems)
 * │
 * └─ New user → write OAuthRegistrationSession to oauth2_registration cookie
 *               → redirect to /complete-profile?returnUrl=...
 * </pre>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Strategy pattern</strong> — implements
 *       {@link AuthenticationSuccessHandler} to customize post-OAuth2 behavior.</li>
 *   <li><strong>Thin handler, thick service</strong> — the handler delegates
 *       token generation and cookie creation to {@link JwtAuthenticationUtils}
 *       and {@link JwtCookieFactory}, keeping its own logic focused on
 *       OAuth2-specific branching.</li>
 *   <li><strong>Local exception handling</strong> — since this handler runs
 *       inside Spring Security's OAuth2 filter (upstream of
 *       {@code ExceptionTranslationFilter}), exceptions like
 *       {@link com.pkmprojects.shoppiq.exception.auth.InvalidOidcUserException}
 *       are caught here and translated into a login-page redirect.</li>
 * </ul>
 *
 * @see OAuthRegistrationCookieService
 * @see JwtCookieFactory
 * @see OAuthReturnUrlFilter
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final UserRepository userRepository;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final JwtCookieFactory jwtCookieFactory;
    private final OAuthRegistrationCookieService registrationCookieService;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${app.security.secure-cookie:true}")
    private boolean secureCookie;

    public OAuth2SuccessHandler(UserRepository userRepository,
                                JwtAuthenticationUtils jwtAuthenticationUtils,
                                JwtCookieFactory jwtCookieFactory,
                                OAuthRegistrationCookieService registrationCookieService) {
        this.userRepository = userRepository;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.jwtCookieFactory = jwtCookieFactory;
        this.registrationCookieService = registrationCookieService;
    }

    /**
     * Processes a successful Google OAuth2 authentication.
     *
     * <p>Extracts and validates the OIDC principal, verifies the email,
     * and branches to either issue a JWT for returning users or store
     * a registration cookie for new users.</p>
     *
     * @param request        the HTTP request during the callback
     * @param response       the HTTP response for cookie or redirect
     * @param authentication the OAuth2 authentication token
     * @throws IOException      if a redirect fails
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        try {
            processAuthenticationSuccess(request, response, authentication);
        } catch (InvalidOidcUserException ex) {
            logger.warn("OAuth2 login rejected: {}", ex.getDetail());
            response.sendRedirect("/login?error=" + ex.getErrorCode().getCode());
        }
    }

    /**
     * Performs the actual OIDC validation and branching logic.
     *
     * <p>Separated from {@link #onAuthenticationSuccess} so that
     * {@link InvalidOidcUserException} thrown by validation can be caught in
     * one place and translated into a login-page redirect. This handler runs
     * inside the OAuth2 login filter, upstream of Spring Security's
     * {@code ExceptionTranslationFilter}, so exceptions must be handled locally.</p>
     *
     * @param request        the HTTP request during the callback
     * @param response       the HTTP response for cookie or redirect
     * @param authentication the OAuth2 authentication token
     * @throws IOException              if a redirect fails
     * @throws InvalidOidcUserException if the principal is missing or the
     *                                  email has not been verified by Google
     */
    private void processAuthenticationSuccess(HttpServletRequest request,
                                              HttpServletResponse response,
                                              Authentication authentication) throws IOException {

        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            logger.error("OAuth2 principal is not an OidcUser instance");
            throw new InvalidOidcUserException("OIDC principal missing.");
        }

        Boolean emailVerified = oidcUser.getClaim("email_verified");
        if (!Boolean.TRUE.equals(emailVerified)) {
            logger.warn("OAuth2 login rejected: email not verified for {}", oidcUser.getEmail());
            throw new InvalidOidcUserException("Google account email is not verified.");
        }

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        logger.info("OAuth2 authentication successful for verified email: {}", email);

        Optional<User> existingUser = userRepository.findUserByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            String jwt = jwtAuthenticationUtils.generateToken(user, expirationTime);
            response.addCookie(jwtCookieFactory.buildJwtCookie(jwt, (int) (expirationTime / 1000)));

            String redirectUrl = extractOAuthReturnUrl(request, response);
            logger.info("Returning OAuth2 user '{}' redirected to {}", user.getUsername(), redirectUrl);
            response.sendRedirect(redirectUrl);
            return;
        }

        // New user — store registration data in a cookie, redirect to complete-profile
        OAuthRegistrationSession registrationSession = new OAuthRegistrationSession(email, name, Instant.now());
        registrationCookieService.save(registrationSession, response);

        String returnTo = extractOAuthReturnUrl(request, response);
        logger.info("New OAuth2 user with email '{}' redirected to /complete-profile (returnTo={})", email, returnTo);
        response.sendRedirect("/complete-profile?returnUrl=" + java.net.URLEncoder.encode(returnTo, java.nio.charset.StandardCharsets.UTF_8));
    }

    private String extractOAuthReturnUrl(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (OAuthReturnUrlFilter.COOKIE_NAME.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setSecure(secureCookie);
                    cookie.setAttribute("SameSite", "Lax");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    if (value.startsWith("/") && !value.startsWith("//")) {
                        return value;
                    }
                    return "/allitems";
                }
            }
        }
        return "/allitems";
    }
}
