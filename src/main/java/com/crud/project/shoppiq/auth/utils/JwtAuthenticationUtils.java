package com.crud.project.shoppiq.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for all JWT operations: token generation, validation, and
 * claim extraction.
 *
 * <p>Uses HMAC-SHA signing with a secret key loaded from
 * {@code application.properties} via the {@code jwt.secret} property.
 * The generated tokens are delivered to clients as HttpOnly cookies (set by
 * the login endpoint) rather than in the response body, so this class has no
 * awareness of the transport mechanism — it only creates and verifies
 * compact JWT strings.</p>
 */
@Component
public class JwtAuthenticationUtils {

    /**
     * Name of the HttpOnly cookie that carries the JWT. Must match {@code JwtAuthenticationFilter}.
     */
    private static final String JWT_COOKIE_NAME = "jwt";

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    /**
     * Initializes the HMAC-SHA signing key after dependency injection.
     * Triggered automatically by the Spring container at startup via
     * {@link PostConstruct}.
     *
     * @throws RuntimeException if the secret byte array is too short for the
     *                          chosen HMAC algorithm
     */
    @PostConstruct
    public void init() {
        try {
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT signing key", e);
        }
    }

    /**
     * Parses a JWT string and returns its claims (payload).
     * Verifies the signature and structural integrity using the secret key.
     *
     * @param token compact JWT string, typically extracted from the
     *              {@code jwt} HttpOnly cookie
     * @return {@link Claims} object containing the subject, expiration, etc.
     * @throws RuntimeException if the token is invalid, expired, or tampered
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Extracts the username (JWT subject) from a token.
     *
     * @param token compact JWT string
     * @return username stored as the subject claim
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Checks whether a token's expiration date is in the past.
     *
     * @param token compact JWT string
     * @return {@code true} if the token is expired, {@code false} if still valid
     * @throws RuntimeException if the expiration claim cannot be read
     */
    public boolean isTokenExpired(String token) {
        try {
            return getClaimsFromToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            throw new RuntimeException("Failed to check token expiration", e);
        }
    }

    /**
     * Validates a token by confirming the subject matches the given user and
     * the token has not expired.
     *
     * @param token       compact JWT string extracted from the {@code jwt} cookie
     * @param userDetails user details loaded from the database
     * @return {@code true} if the username matches and the token is still valid
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            return userDetails.getUsername().equals(getUsernameFromToken(token))
                    && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates a signed JWT token for an authenticated user.
     *
     * <p>The returned string is meant to be written into an HttpOnly cookie by
     * the login endpoint, not returned in the response body. Cookie attributes
     * should be set by the caller:</p>
     * <pre>
     * Set-Cookie: jwt=&lt;token&gt;; HttpOnly; Secure; SameSite=Strict; Path=/
     * </pre>
     * <p>When "Remember Me" is active the caller should additionally set
     * {@code Max-Age} to match the {@code expiration} value passed here.</p>
     *
     * @param userDetails authenticated user details (username used as subject)
     * @param expiration  token lifetime in milliseconds from now
     * @return compact signed JWT string
     * @throws RuntimeException if token generation fails
     */
    public String generateToken(UserDetails userDetails, long expiration) {
        try {
            return Jwts.builder()
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * Builds a JWT cookie with the security attributes used consistently across
     * login and logout.
     *
     * @param value  JWT token string on login; empty string on logout
     * @param maxAge {@code -1} = session cookie (no Max-Age header);
     *               {@code 0}  = expire immediately (browser deletes cookie);
     *               {@code > 0} = persistent cookie surviving browser restarts
     * @return fully configured {@link Cookie} ready to be added to the response
     */
    public Cookie buildJwtCookie(String value, int maxAge) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, value);
        cookie.setHttpOnly(true);            // not readable by document.cookie
        cookie.setSecure(false);             // HTTP for dev;
        cookie.setPath("/");                 // attached to every request on this origin
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");  // CSRF mitigation for modern browsers
        return cookie;
    }
}