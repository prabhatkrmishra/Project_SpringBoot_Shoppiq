package com.pkmprojects.shoppiq.auth.service;

import com.pkmprojects.shoppiq.auth.security.SecurityUser;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user details from the database for password-based authentication.
 *
 * <p>Bridges the database and the authentication provider by implementing
 * {@link UserDetailsService}. Returns a {@link SecurityUser} wrapper for
 * the domain entity.</p>
 *
 * @author prabhatkrmishra
 * @see SecurityUser
 * @see com.pkmprojects.shoppiq.auth.service.AuthService
 * @since 1.0.0
 */
@Service
public class CustomUserDetailService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailService.class);

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username for credential verification during login.
     *
     * @param username the username from the login form
     * @return {@link SecurityUser} wrapping the domain entity
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    logger.debug("User not found during login: {}", username);
                    return new UsernameNotFoundException("Invalid credentials");
                });
        return new SecurityUser(user);
    }
}
