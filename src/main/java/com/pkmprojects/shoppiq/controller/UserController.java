package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.user.UserRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRequest newUserRequest) {
        userService.createUser(newUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal User user) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("username", user.getUsername());
        profile.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return ResponseEntity.ok(profile);
    }
}
