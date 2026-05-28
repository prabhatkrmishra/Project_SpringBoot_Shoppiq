package com.crud.project.shoppiq.controllers;

import com.crud.project.shoppiq.dto.user.UserRequest;
import com.crud.project.shoppiq.models.User;
import com.crud.project.shoppiq.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    final UserService userService;

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
    public ResponseEntity<String> registerUser(@RequestBody UserRequest newUserRequest) {
        boolean isCreated = userService.createUser(newUserRequest);
        if (isCreated)
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to register user");
    }
}
