package com.crud.project.shoppiq.services;

import com.crud.project.shoppiq.dto.user.UserRequest;
import com.crud.project.shoppiq.models.User;
import com.crud.project.shoppiq.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserService {
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean createUser(UserRequest newUserRequest) {
        try {
            User newUser = new User();

            newUser.setUsername(newUserRequest.getUsername());
            newUser.setPassword(passwordEncoder.encode(newUserRequest.getPassword()));

            userRepository.save(newUser);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
