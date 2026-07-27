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
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 *
 * <p>Used during username/password login by {@code DaoAuthenticationProvider}
 * to load the user for credential verification. Returns a {@link SecurityUser}
 * adapter wrapping the domain {@link User} entity.</p>
 *
 * <p>Not used during JWT authentication — the JWT filter loads the user by
 * ID directly from the repository.</p>
 *
 * @see SecurityUser
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
