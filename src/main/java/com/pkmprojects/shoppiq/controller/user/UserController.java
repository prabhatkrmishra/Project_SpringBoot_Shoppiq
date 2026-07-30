package com.pkmprojects.shoppiq.controller.user;

import com.pkmprojects.shoppiq.dto.user.ChangePasswordRequest;
import com.pkmprojects.shoppiq.dto.user.UpdateProfileRequest;
import com.pkmprojects.shoppiq.dto.user.UserRequest;
import com.pkmprojects.shoppiq.dto.user.UserResponse;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authenticated user profile operations.
 *
 * <p>Handles registration, profile retrieval, profile updates, and password
 * changes for the currently authenticated user. Registration is a public
 * endpoint available to unauthenticated visitors. All other endpoints require
 * authentication and operate on the authenticated user's own profile.</p>
 *
 * <p>This controller acts as the HTTP boundary for user profile operations. It
 * delegates all business logic — account creation, profile retrieval, profile
 * updates, and password changes — to {@link UserService}. The controller handles
 * no business logic beyond request validation and response assembly.</p>
 *
 * <p>Registration is unauthenticated. Profile and password endpoints require
 * any authenticated role. All endpoints are mounted under /user.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /user/register    — register a new user account (public)
 * GET    /user/profile     — get the authenticated user's profile
 * PUT    /user/profile     — update the authenticated user's profile
 * PUT    /user/password    — change the authenticated user's password
 * </pre>
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
     * must include a valid email, username, and password. A new CUSTOMER
     * role is assigned by default.</p>
     *
     * @param newUserRequest the user registration payload (validated via @Valid)
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
     * @param user the authenticated user resolved from the JWT
     * @return 200 OK with the user profile response
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(userService.getProfile(user));
    }

    /**
     * Updates the authenticated user's profile information.
     *
     * @param user    the authenticated user resolved from the JWT
     * @param request the updated profile data (validated via @Valid)
     * @return 200 OK with the updated user profile response
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
     * <p>Validates the current password before applying the new one.
     * The new password must meet the configured complexity requirements.</p>
     *
     * @param user    the authenticated user resolved from the JWT
     * @param request the current and new password payload (validated via @Valid)
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