package com.crud.project.shoppiq.auth.service;

import com.crud.project.shoppiq.auth.dto.JwtRequest;
import com.crud.project.shoppiq.auth.dto.JwtResponse;
import com.crud.project.shoppiq.auth.utils.JwtAuthenticationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Service handling authentication logic and JWT token creation.
 * Orchestrates the login flow: credential validation and token generation.
 */
@Service
public class AuthService {

    // value is in ms
    @Value("${jwt.expiration}")
    private long expirationTime;
    @Value("${jwt.short-expiration}")
    private long shortExpiration;

    private final AuthenticationManager authManager;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final UserDetailsService userDetailsService;

    public AuthService(AuthenticationManager authManager, JwtAuthenticationUtils jwtAuthenticationUtils, UserDetailsService userDetailsService) {
        this.authManager = authManager;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Performs credential verification using Spring Security's AuthenticationManager.
     * Creates a UsernamePasswordAuthenticationToken containing the supplied username
     * and password, then delegates authentication to AuthenticationManager.
     * <p>
     * AuthenticationManager uses UserDetailsService to load the user from the database,
     * PasswordEncoder to verify the password, and retrieves the user's roles/authorities
     * for authorization after successful authentication.
     *
     * @param username user's login name
     * @param password plain-text password
     * @throws BadCredentialsException if authentication fails
     */
    private void authenticate(String username, String password) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password", ex);
        }
    }

    /**
     * Complete login workflow: validate credentials, load user details, generate JWT.
     * Called by FrontEndController.login().
     *
     * @param request JwtRequest containing username and password
     * @return JwtResponse containing signed JWT token
     * @throws RuntimeException if any step fails
     */
    public JwtResponse login(JwtRequest request) {
        try {
            // 1. Authenticate with authentication managaer
            authenticate(request.getUsername(), request.getPassword());

            // 2. Generate token with expiration time
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            long expiration = Boolean.TRUE.equals(request.getRememberMe()) ? expirationTime : shortExpiration;
            String token = jwtAuthenticationUtils.generateToken(userDetails, expiration);

            // 3. Return token as response
            return new JwtResponse(token);
        } catch (Exception ex) {
            throw new RuntimeException("Login failed", ex);
        }
    }
}
