package com.crud.project.shoppiq.auth.controller;

import com.crud.project.shoppiq.auth.dto.JwtRequest;
import com.crud.project.shoppiq.auth.dto.JwtResponse;
import com.crud.project.shoppiq.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 * Serves as the entry point for login, logout, and JWT cookie management.
 *
 * <p>The JWT is never returned in the response body. Instead, {@link AuthService}
 * writes it directly into an HttpOnly cookie via the {@link HttpServletResponse}
 * passed through from here, keeping the token inaccessible to JavaScript.</p>
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
     * Delegates credential validation and JWT cookie creation to {@link AuthService}.
     * On success the JWT is written as an HttpOnly cookie on the response; the body
     * contains only a status message.
     *
     * @param jwtRequest contains username, password, and rememberMe flag
     * @param response   servlet response used by the service to set the JWT cookie
     * @return 200 with a {@link JwtResponse} status message, or propagates an
     * exception on failure (handled by your exception handler)
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest jwtRequest,
                                             HttpServletResponse response) {
        try {
            return ResponseEntity.ok(authService.login(jwtRequest, response));
        } catch (Exception e) {
            throw new RuntimeException("Login request failed", e);
        }
    }

    /**
     * Handles user logout requests.
     * Delegates cookie expiration to {@link AuthService}, which overwrites the JWT
     * cookie with {@code Max-Age=0} so the browser deletes it immediately.
     *
     * <p>This endpoint is required because the JWT cookie is HttpOnly — JavaScript
     * cannot read or delete it, so the browser relies on the server to expire it.</p>
     *
     * @param response servlet response used by the service to expire the JWT cookie
     * @return 200 with no body
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().build();
    }
}