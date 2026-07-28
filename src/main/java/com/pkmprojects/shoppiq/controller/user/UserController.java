package com.pkmprojects.shoppiq.controller.user;

import com.pkmprojects.shoppiq.dto.user.UserResponse;
import com.pkmprojects.shoppiq.dto.user.ChangePasswordRequest;
import com.pkmprojects.shoppiq.dto.user.UpdateProfileRequest;
import com.pkmprojects.shoppiq.dto.user.UserRequest;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <strong>Spring Boot Concept:</strong> REST controller for authenticated user profile operations.
 *
 * <p>Handles registration, profile retrieval, profile updates, and password
 * changes for the currently authenticated user. Registration is public;
 * all other endpoints require authentication.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Thin controller</strong> — no business logic; validates input and delegates to service layer.</li>
 *   <li><strong>Self-service</strong> — authenticated endpoints operate on the user's own profile only.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see UserService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user account.
     *
     * <p>This endpoint is public (no authentication required). The request
     * must include a valid email, username, and password.</p>
     *
     * @param newUserRequest the user registration payload
     * @return 201 Created with a success message
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRequest newUserRequest) {
        userService.createUser(newUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    /**
     * Returns the authenticated user's profile.
     *
     * @param user the authenticated user (from JWT)
     * @return 200 OK with the user profile
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(userService.getProfile(user));
    }

    /**
     * Updates the authenticated user's profile information.
     *
     * @param user    the authenticated user (from JWT)
     * @param request the updated profile data
     * @return 200 OK with the updated user profile
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        userService.updateProfile(user, request);
        return ResponseEntity.ok(userService.getProfile(user));
    }

    /**
     * Changes the authenticated user's password.
     *
     * <p>Validates the current password before applying the new one.</p>
     *
     * @param user    the authenticated user (from JWT)
     * @param request the current and new password payload
     * @return 200 OK
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(user, request);
        return ResponseEntity.ok().build();
    }

}