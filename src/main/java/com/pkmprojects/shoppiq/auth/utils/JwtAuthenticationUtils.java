package com.pkmprojects.shoppiq.auth.utils;

import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.security.SecurityUser;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.auth.JwtAuthenticationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Utility class for all JWT operations: token generation, validation,
 * claim extraction, and cookie parsing.
 *
 * <h3>JWT / Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>HMAC-SHA256 signing</strong> — tokens are signed with a
 *       symmetric key (the {@code jwt.secret} property) using
 *       {@code io.jsonwebtoken} (JJWT). The same secret is used for both
 *       signing and verification.</li>
 *   <li><strong>Custom JWT claims</strong> — beyond standard claims
 *       ({@code sub}, {@code iat}, {@code exp}), the token carries
 *       application-specific claims: {@code userId} (for DB lookups),
 *       {@code roles} (for authorization, avoiding per-request role queries),
 *       and {@code tokenVersion} (for forced token invalidation).</li>
 *   <li><strong>Token version invalidation</strong> — each user has a
 *       {@code tokenVersion} column in the database. When the password is
 *       changed or an admin forces logout, incrementing this version
 *       immediately invalidates all existing JWTs, even if they haven't
 *       expired yet.</li>
 *   <li><strong>Stateless token design</strong> — the JWT carries enough
 *       information to build a complete {@code SecurityContext} (including
 *       roles) with only one database query per request (to verify token
 *       version and enabled status). No HTTP session is created or read.</li>
 *   <li><strong>Refresh token validation</strong> — {@link #validateTokenForRefresh}
 *       checks the token's age from issuance (not expiration), enabling the
 *       refresh endpoint to accept recently-expired tokens while rejecting
 *       old ones.</li>
 * </ul>
 *
 * <h3>Token claims</h3>
 * <ul>
 *   <li>{@code sub} — username for identification</li>
 *   <li>{@code userId} — user ID for entity references</li>
 *   <li>{@code roles} — list of granted authority strings (e.g.,
 *       "ROLE_CUSTOMER", "ROLE_ADMIN") for authorization decisions</li>
 *   <li>{@code tokenVersion} — must match the user's current token version
 *       in the database for the token to be valid.</li>
 *   <li>{@code iat} — issued-at timestamp</li>
 *   <li>{@code exp} — expiration timestamp</li>
 * </ul>
 *
 * <h3>Validation checks (in order)</h3>
 * <ol>
 *   <li>JWT signature verified using HMAC-SHA secret key (handled by JJWT's
 *       {@code parser().verifyWith(key).build().parseSignedClaims()})</li>
 *   <li>Token has not expired ({@code exp} claim checked by JJWT)</li>
 *   <li>Token version matches the user's current version in the database</li>
 *   <li>User account is enabled</li>
 *   <li>Username in token matches the database username</li>
 * </ol>
 *
 * <h3>Request flow</h3>
 * <pre>
 * JwtAuthenticationFilter receives request
 *       ↓
 * Extract JWT from cookie
 *       ↓
 * Parse claims: username, userId, roles, tokenVersion
 *       ↓
 * Load User from database to check tokenVersion and enabled status
 *       ↓
 * tokenVersion matches AND user enabled? → Build authentication from claims
 *       ↓
 * Set SecurityContext with roles from JWT (no further DB queries)
 * </pre>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Utility / Helper pattern</strong> — stateless methods grouped
 *       in a {@code @Component} that can be injected anywhere JWT operations
 *       are needed (filter, service, handler).</li>
 *   <li><strong>Fail-closed validation</strong> — {@link #validateToken} and
 *       {@link #validateTokenForRefresh} return {@code false} for any
 *       exception, ensuring that unexpected errors never accidentally grant
 *       access.</li>
 *   <li><strong>Claim-based authority resolution</strong> — roles are extracted
 *       from the JWT claims rather than queried from the database on every
 *       request, reducing database load and latency for authorization checks.</li>
 * </ul>
 *
 * @see JwtCookieFactory
 * @see JwtAuthenticationFilter
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationUtils.class);

    private static final String JWT_COOKIE_NAME = "jwt";

    private final SecretKey key;

    public JwtAuthenticationUtils(@Value("${jwt.secret}") String secret) {
        try {
            this.key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT signing key", e);
        }
    }

    /**
     * Parses a JWT string and returns its claims.
     * Verifies the signature and structural integrity using the secret key.
     *
     * @param token compact JWT string extracted from the {@code jwt} cookie
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
     * Extracts the username from the token's subject claim.
     *
     * @param token compact JWT string
     * @return username stored as the subject
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Extracts the user ID from the token.
     * Used for entity references with a single database lookup for token validation.
     *
     * @param token compact JWT string
     * @return user ID
     */
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }

    /**
     * Extracts the roles list from the token.
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
     * Extracts the token version from the token.
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
        return getClaimsFromToken(token).getExpiration().toInstant().isBefore(Instant.now());
    }

    /**
     * Validates a token by checking the token version, account status,
     * and username against the database.
     *
     * <p>Loads the user by ID from the token claims and verifies:
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
     * @param token compact JWT string extracted from the cookie
     * @param user  the user loaded from the database by user ID
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
                    .claim("roles", new SecurityUser(user).getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList())
                    .claim("tokenVersion", user.getTokenVersion())
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusMillis(expiration)))
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
     * Verifies signature, token version, account status, and token age.
     * Expired tokens are rejected by JJWT's parseSignedClaims before
     * application-level checks run.
     *
     * @param token        compact JWT string extracted from the cookie
     * @param user         the user loaded from the database by user ID
     * @param maxAgeMillis maximum allowed token age (from issuance) in milliseconds
     * @return {@code true} if the token is valid for refresh
     */
    public boolean validateTokenForRefresh(String token, User user, long maxAgeMillis) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenUsername = claims.getSubject();
            boolean usernameMatches = user.getUsername().equals(tokenUsername);

            Integer tokenTokenVersion = claims.get("tokenVersion", Integer.class);
            boolean tokenVersionMatches = tokenTokenVersion != null
                    && tokenTokenVersion.equals(user.getTokenVersion());

            boolean userEnabled = user.isEnabled();

            Instant issuedAt = claims.getIssuedAt().toInstant();
            long ageMillis = Instant.now().toEpochMilli() - issuedAt.toEpochMilli();
            boolean ageOk = ageMillis <= maxAgeMillis;
            if (!ageOk) {
                logger.debug("Token age {}ms exceeds max allowed {}ms", ageMillis, maxAgeMillis);
            }

            return usernameMatches && tokenVersionMatches && userEnabled && ageOk;
        } catch (Exception e) {
            logger.debug("Token refresh validation failed: {}", e.getMessage());
            return false;
        }
    }

}
