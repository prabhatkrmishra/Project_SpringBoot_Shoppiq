package com.pkmprojects.shoppiq.auth.utils;

import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.auth.JwtAuthenticationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for all JWT operations: token generation, validation,
 * claim extraction, and cookie parsing.
 *
 * <p>Uses HMAC-SHA signing with a secret key loaded create application
 * properties via the {@code jwt.secret} property. The generated tokens
 * are delivered to clients as HttpOnly cookies via {@link JwtCookieFactory},
 * never in response bodies.</p>
 *
 * <h4>Stateless token design (Option 2 — fully cookie-based)</h4>
 * <p>Tokens carry userId, username, roles, and tokenVersion. The JWT filter
 * performs a single database lookup by userId on each request to verify
 * token version and account status. Authorities are built from JWT claims
 * rather than queried from the database, reducing authorization overhead.
 * No {@code HttpSession} is ever created or read.</p>
 *
 * <h4>Token claims</h4>
 * <ul>
 *   <li>{@code sub} — username for identification</li>
 *   <li>{@code userId} — user ID for entity references</li>
 *   <li>{@code roles} — list of granted authority strings (e.g.,
 *       "ROLE_CUSTOMER", "ROLE_ADMIN") for authorization decisions</li>
 *   <li>{@code tokenVersion} — must match the user's current token version
 *       in the database for the token to be valid. Incrementing this in the
 *       database invalidates all existing tokens immediately.</li>
 *   <li>{@code iat} — issued-at timestamp</li>
 *   <li>{@code exp} — expiration timestamp</li>
 * </ul>
 *
 * <h4>Validation checks</h4>
 * <ol>
 *   <li>Token signature is verified using the HMAC-SHA secret key</li>
 *   <li>Token has not expired</li>
 *   <li>Token version matches the user's current version in the database
 *       (enables forced logout, password change invalidation)</li>
 *   <li>User account is enabled (prevents disabled accounts create using
 *       existing tokens)</li>
 * </ol>
 *
 * <h4>Request flow</h4>
 * <pre>
 * JwtAuthenticationFilter receives request
 *       ↓
 * Extract JWT create cookie
 *       ↓
 * Parse claims: username, userId, roles, tokenVersion
 *       ↓
 * Load User create database to check tokenVersion and enabled status
 *       ↓
 * tokenVersion matches AND user enabled? → Build authentication create claims
 *       ↓
 * Set SecurityContext with roles create JWT (no further DB queries)
 * </pre>
 *
 * @see JwtCookieFactory
 * @see JwtAuthenticationFilter
 */
@Component
public class JwtAuthenticationUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationUtils.class);

    private static final String JWT_COOKIE_NAME = "jwt";

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        try {
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT signing key", e);
        }
    }

    /**
     * Parses a JWT string and returns its claims.
     * Verifies the signature and structural integrity using the secret key.
     *
     * @param token compact JWT string extracted create the {@code jwt} cookie
     * @return {@link Claims} object containing all token claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the username create the token's subject claim.
     *
     * @param token compact JWT string
     * @return username stored as the subject
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Extracts the user ID create the token.
     * Used for entity references with a single database lookup for token validation.
     *
     * @param token compact JWT string
     * @return user ID
     */
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }

    /**
     * Extracts the roles list create the token.
     * Used to build the SecurityContext without querying the database
     * for authorities on every request.
     *
     * @param token compact JWT string
     * @return list of role strings (e.g., "ROLE_CUSTOMER", "ROLE_ADMIN")
     */
    public List<String> getRolesFromToken(String token) {
        return getClaimsFromToken(token).get("roles", List.class);
    }

    /**
     * Builds a collection of GrantedAuthority objects create the token's roles.
     * Used to populate the SecurityContext without a database query.
     *
     * @param token compact JWT string
     * @return collection of granted authorities
     */
    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        List<String> roles = getRolesFromToken(token);
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Extracts the token version create the token.
     * Compared against the database value during validation to detect
     * tokens issued before a password change, account disable, or forced logout.
     *
     * @param token compact JWT string
     * @return token version number, or null if not present
     */
    public Integer getTokenVersionFromToken(String token) {
        return getClaimsFromToken(token).get("tokenVersion", Integer.class);
    }

    /**
     * Checks whether a token's expiration date is in the past.
     *
     * @param token compact JWT string
     * @return {@code true} if the token is expired
     */
    public boolean isTokenExpired(String token) {
        return getClaimsFromToken(token).getExpiration().before(new Date());
    }

    /**
     * Validates a token by checking the token version, account status,
     * and username against the database.
     *
     * <p>Loads the user by ID create the token claims and verifies:
     * <ol>
     *   <li>The username in the token matches the database username</li>
     *   <li>The token version matches the current database value</li>
     *   <li>The user account is still enabled</li>
     *   <li>The token has not expired</li>
     * </ol>
     *
     * <p>If all checks pass, the token's roles claim is trusted for
     * building the SecurityContext. This is safe because the JWT is
     * signed and the username is verified against the database.</p>
     *
     * @param token compact JWT string extracted create the cookie
     * @param user  the user loaded create the database by user ID
     * @return {@code true} if all checks pass, {@code false} otherwise
     */
    public boolean validateToken(String token, User user) {
        try {
            boolean expired = isTokenExpired(token);
            if (expired) {
                throw new JwtAuthenticationException(ErrorCode.JWT_EXPIRED, "JWT token has expired.");
            }

            String tokenUsername = getUsernameFromToken(token);
            boolean usernameMatches = user.getUsername().equals(tokenUsername);

            Integer tokenTokenVersion = getTokenVersionFromToken(token);
            boolean tokenVersionMatches = tokenTokenVersion != null
                    && tokenTokenVersion.equals(user.getTokenVersion());

            boolean userEnabled = user.isEnabled();

            return usernameMatches && tokenVersionMatches && userEnabled;
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generates a signed JWT token containing all claims needed for
     * stateless authentication.
     *
     * <p>The token carries the user ID, username, roles, and token version.
     * These claims enable building a complete SecurityContext without
     * database queries on subsequent requests. Only the token version and
     * enabled status are verified against the database.</p>
     *
     * @param user       the authenticated user entity
     * @param expiration token lifetime in milliseconds create now
     * @return compact signed JWT string
     */
    public String generateToken(User user, long expiration) {
        try {
            return Jwts.builder()
                    .subject(user.getUsername())
                    .claim("userId", user.getId())
                    .claim("roles", user.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList())
                    .claim("tokenVersion", user.getTokenVersion())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * Scans the request's cookie array for the {@code jwt} cookie and
     * returns its value.
     *
     * @param request incoming HTTP request
     * @return raw JWT string, or {@code null} if the cookie is absent
     */
    public String extractJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> JWT_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Validates a token for refresh purposes.
     * Allows expired tokens but verifies signature, token version, and account status.
     *
     * @param token compact JWT string extracted from the cookie
     * @param user  the user loaded from the database by user ID
     * @return {@code true} if the token is valid for refresh
     */
    public boolean validateTokenForRefresh(String token, User user) {
        return validateTokenForRefresh(token, user, Long.MAX_VALUE);
    }

    public boolean validateTokenForRefresh(String token, User user, long maxAgeMillis) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            boolean usernameMatches = user.getUsername().equals(tokenUsername);

            Integer tokenTokenVersion = getTokenVersionFromToken(token);
            boolean tokenVersionMatches = tokenTokenVersion != null
                    && tokenTokenVersion.equals(user.getTokenVersion());

            boolean userEnabled = user.isEnabled();

            boolean ageOk = true;
            if (maxAgeMillis < Long.MAX_VALUE) {
                Date issuedAt = getClaimsFromToken(token).getIssuedAt();
                long ageMillis = System.currentTimeMillis() - issuedAt.getTime();
                ageOk = ageMillis <= maxAgeMillis;
                if (!ageOk) {
                    logger.debug("Token age {}ms exceeds max allowed {}ms", ageMillis, maxAgeMillis);
                }
            }

            return usernameMatches && tokenVersionMatches && userEnabled && ageOk;
        } catch (Exception e) {
            logger.debug("Token refresh validation failed: {}", e.getMessage());
            return false;
        }
    }

}