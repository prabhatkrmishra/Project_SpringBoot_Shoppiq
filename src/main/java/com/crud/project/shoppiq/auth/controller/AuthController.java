package com.crud.project.shoppiq.auth.controller;

import com.crud.project.shoppiq.auth.dto.JwtRequest;
import com.crud.project.shoppiq.auth.dto.JwtResponse;
import com.crud.project.shoppiq.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 * Serves as entry point for login and token generation.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles user login requests.
     * Receives username/password, validates them, and returns a JWT token.
     *
     * @param jwtRequest contains username and password
     * @return ResponseEntity with JWT token inside JwtResponse
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest jwtRequest) {
        try {
            return ResponseEntity.ok(authService.login(jwtRequest));
        } catch (Exception e) {
            throw new RuntimeException("Login request failed", e);
        }
    }
}