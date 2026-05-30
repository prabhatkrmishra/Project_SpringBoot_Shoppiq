package com.crud.project.shoppiq.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for all JWT operations: token generation, validation, claim extraction.
 * Uses HMAC-SHA signing with a secret key loaded from application properties.
 */
@Component
public class JwtAuthenticationUtils {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    /**
     * Initializes the signing key after dependency injection.
     * Triggered automatically by Spring container at startup.
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
     * Parses a JWT token and returns its claims (payload).
     * Verifies signature and integrity using the secret key.
     *
     * @param token JWT string
     * @return Claims object containing subject, expiration, etc.
     * @throws RuntimeException if token is invalid or tampered
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Extracts username (subject) from JWT claims.
     *
     * @param token JWT string
     * @return username stored in token
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Checks whether the token's expiration date is in the past.
     *
     * @param token JWT string
     * @return true if expired, false if still valid
     */
    public boolean isTokenExpired(String token) {
        try {
            return getClaimsFromToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            throw new RuntimeException("Failed to check token expiration", e);
        }
    }

    /**
     * Validates token by comparing username and expiration status.
     *
     * @param token       JWT string
     * @param userDetails loaded user details from database
     * @return true if username matches and token is not expired
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            return userDetails.getUsername().equals(getUsernameFromToken(token)) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates a signed JWT token for an authenticated user.
     * Uses modern JJWT builder (subject, issuedAt, expiration).
     *
     * @param userDetails authenticated user details
     * @param expiration  token expiration time in milliseconds
     * @return compact JWT string
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
}