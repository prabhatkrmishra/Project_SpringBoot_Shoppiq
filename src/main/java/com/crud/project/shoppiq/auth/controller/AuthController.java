package com.crud.project.shoppiq.auth.controller;

import com.crud.project.shoppiq.auth.dto.JwtRequest;
import com.crud.project.shoppiq.auth.dto.JwtResponse;
import com.crud.project.shoppiq.auth.dto.OauthRequestDto;
import com.crud.project.shoppiq.auth.service.AuthService;
import com.crud.project.shoppiq.auth.dto.OauthResponseDto;
import com.crud.project.shoppiq.dto.user.UserRequest;
import com.crud.project.shoppiq.repositories.UserRepository;
import com.crud.project.shoppiq.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserService userService, UserRepository userRepository) {
        this.authService = authService;
        this.userService = userService;
        this.userRepository = userRepository;
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

    @GetMapping("/google/get-profile")
    public ResponseEntity<OauthResponseDto> getOauthProfile(HttpSession session) {

        OauthResponseDto oauthResponseDto = (OauthResponseDto) session.getAttribute("oauth_user");
        if (oauthResponseDto == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(oauthResponseDto);
    }

    @PostMapping("/google/complete-profile")
    public ResponseEntity<String> completeProfile(@RequestBody OauthRequestDto newRequest,
                                                  HttpSession session, HttpServletResponse response) {

        OauthResponseDto oauthUser = (OauthResponseDto) session.getAttribute("oauth_user");
        if (oauthUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OAuth session expired");
        }

        if (userRepository.findUserByEmail(oauthUser.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email already registered");
        }

        if (userRepository.findUserByUsername(newRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Username already exists");
        }

        UserRequest newGoogleUserRequest = new UserRequest();
        newGoogleUserRequest.setName(oauthUser.getName());
        newGoogleUserRequest.setEmail(oauthUser.getEmail());
        newGoogleUserRequest.setUsername(newRequest.getUsername());
        newGoogleUserRequest.setPassword(newRequest.getPassword());

        boolean isCreated = userService.createUser(newGoogleUserRequest);
        if (isCreated) {
            JwtRequest jwtRequest = new JwtRequest();
            jwtRequest.setUsername(newRequest.getUsername());
            jwtRequest.setPassword(newRequest.getPassword());

            /*
             * Registration completed.
             * Remove temporary OAuth registration data.
             */
            session.removeAttribute("oauth_user");

            authService.login(jwtRequest, response);

            /*
             * Registration flow is complete.
             * Destroy temporary OAuth session.
             */
            session.invalidate();

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to register user");
    }
}