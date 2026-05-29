package com.crud.project.shoppiq.auth.service;

import com.crud.project.shoppiq.auth.dto.JwtRequest;
import com.crud.project.shoppiq.auth.dto.JwtResponse;
import com.crud.project.shoppiq.auth.jwt.JwtAuthenticationUtils;
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

    private final AuthenticationManager authManager;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final UserDetailsService userDetailsService;

    public AuthService(AuthenticationManager authManager, JwtAuthenticationUtils jwtAuthenticationUtils, UserDetailsService userDetailsService) {
        this.authManager = authManager;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Performs actual credential verification using Spring Security's AuthenticationManager.
     * Delegates to DaoAuthenticationProvider which calls UserDetailsService and PasswordEncoder.
     *
     * @param username user's login name
     * @param password plain-text password
     * @throws BadCredentialsException if authentication fails
     */
    private void authenticate(String username, String password) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password", ex);
        }
    }

    /**
     * Complete login workflow: validate credentials, load user details, generate JWT.
     * Called by AuthController.login().
     *
     * @param request JwtRequest containing username and password
     * @return JwtResponse containing signed JWT token
     * @throws RuntimeException if any step fails
     */
    public JwtResponse login(JwtRequest request) {
        try {
            // 1. Authenticate with authentication managaer
            authenticate(request.getUsername(), request.getPassword());

            // 2. Generate token
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtAuthenticationUtils.generateToken(userDetails);

            // 3. Return token as response
            return new JwtResponse(token);
        } catch (Exception ex) {
            throw new RuntimeException("Login failed", ex);
        }
    }
}
