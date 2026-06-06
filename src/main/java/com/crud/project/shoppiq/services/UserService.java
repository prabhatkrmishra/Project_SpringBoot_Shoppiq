package com.crud.project.shoppiq.services;

import com.crud.project.shoppiq.dto.user.UserRequest;
import com.crud.project.shoppiq.models.Role;
import com.crud.project.shoppiq.models.User;
import com.crud.project.shoppiq.repositories.RolesRepository;
import com.crud.project.shoppiq.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolesService rolesService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RolesRepository roleRepository, RolesService rolesService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolesService = rolesService;
    }

    public boolean createUser(UserRequest newUserRequest) {
        try {
            User newUser = new User();

            newUser.setName(newUserRequest.getName());
            newUser.setEmail(newUserRequest.getEmail());
            newUser.setUsername(newUserRequest.getUsername());
            newUser.setPassword(passwordEncoder.encode(newUserRequest.getPassword()));
            newUser.setRoles(Set.of(rolesService.getCustomerRole()));

            userRepository.save(newUser);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
