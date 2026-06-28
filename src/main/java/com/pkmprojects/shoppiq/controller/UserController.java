package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.user.UserRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRequest newUserRequest) {
        userService.createUser(newUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}
