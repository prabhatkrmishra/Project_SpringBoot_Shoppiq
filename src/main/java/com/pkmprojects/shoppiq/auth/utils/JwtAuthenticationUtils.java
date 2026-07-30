package com.pkmprojects.shoppiq.auth.utils;

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
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Utility class for JWT operations: generation, validation, and claim extraction.
 *
 * <p>This component provides HMAC-SHA256 signed JWT tokens with custom claims
 * for stateless authentication. It handles token generation with configurable
 * expiration, token validation (signature, expiration, token version), and
 * claim extraction for user identity and roles. The token version claim
 * supports forced logout by invalidating all existing tokens when the
 * version is incremented.</p>
 *
 * <p>The utility is used by the {@link com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter}
 * for token validation and by the {@link com.pkmprojects.shoppiq.auth.service.AuthService}
 * for token generation. It also provides methods for extracting user
 * information from HTTP requests, which is used by the rate limit filter
 * and other components that need to identify the current user.</p>
 *
 * @author prabhatkrmishra
 * @see JwtCookieFactory
 * @see com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationUtils.class);

    private static final String JWT_COOKIE_NAME = "jwt";

    private final SecretKey key;
    private final Clock clock;

    public JwtAuthenticationUtils(@Value("${jwt.secret}") String secret, Clock clock) {
        this.clock = clock;
        try {
            this.key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_JWT, "Failed to initialize JWT signing key");
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
     * Extracts both userId and username from a single JWT parse.
     * Use this instead of calling {@link #getUserIdFromToken} and
     * {@link #getUsernameFromToken} separately to avoid parsing the
     * JWT twice.
     *
     * @param token compact JWT string
     * @return array of [userId, username]; userId may be null if claim is missing
     */
    public Object[] extractUserIdAndUsername(String token) {
        Claims claims = getClaimsFromToken(token);
        return new Object[]{
                claims.get("userId", Long.class),
                claims.getSubject()
        };
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
        return getClaimsFromToken(token).getExpiration().toInstant().isBefore(Instant.now(clock));
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
            Claims claims = getClaimsFromToken(token);

            boolean expired = claims.getExpiration().toInstant().isBefore(Instant.now(clock));
            if (expired) {
                throw new JwtAuthenticationException(ErrorCode.JWT_EXPIRED, "JWT token has expired.");
            }

            String tokenUsername = claims.getSubject();
            boolean usernameMatches = user.getUsername().equals(tokenUsername);

            Integer tokenTokenVersion = claims.get("tokenVersion", Integer.class);
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
                    .issuedAt(Date.from(Instant.now(clock)))
                    .expiration(Date.from(Instant.now(clock).plusMillis(expiration)))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_JWT, "Token generation failed");
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
            long ageMillis = Instant.now(clock).toEpochMilli() - issuedAt.toEpochMilli();
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
