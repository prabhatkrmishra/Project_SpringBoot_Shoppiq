package com.crud.project.shoppiq.auth.service;

import com.crud.project.shoppiq.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * Bridges the application's UserRepository with Spring Security.
 * Used during login authentication and JWT token validation.
 */
@Service
public class ShoppiqUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public ShoppiqUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username from the database.
     * Called automatically by DaoAuthenticationProvider (login) and JwtAuthenticationFilter (token validation).
     *
     * @param username the username to search for
     * @return UserDetails object expected by Spring Security
     * @throws UsernameNotFoundException if user does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userRepository.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } catch (Exception e) {
            throw new UsernameNotFoundException("Database error while loading user", e);
        }
    }
}