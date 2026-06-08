package com.crud.project.shoppiq.auth.service;

import com.crud.project.shoppiq.repositories.UserRepository;
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
 * to load the user for credential verification. The returned {@link UserDetails}
 * includes the hashed password, roles, and account status needed for
 * authentication decisions.</p>
 *
 * <p>Not used during JWT authentication — the JWT filter loads the user by
 * ID directly from the repository and validates token version and enabled
 * status without going through this service.</p>
 *
 * <p>Flow path during login:</p>
 * <pre>
 * AuthService.authenticate()
 *       ↓
 * AuthenticationManager.authenticate()
 *       ↓
 * DaoAuthenticationProvider.loadUserByUsername()
 *       ↓
 * CustomUserDetailService.loadUserByUsername()
 *       ↓
 * UserRepository.findUserByUsername()
 *       ↓
 * Found → return UserDetails with password hash → password comparison
 * Not found → UsernameNotFoundException → 401
 * </pre>
 *
 * @see com.crud.project.shoppiq.auth.service.AuthService
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
     * <p>Database errors propagate naturally — a database outage is not
     * "user not found" and should be handled by the global exception handler.</p>
     *
     * @param username the username from the login form
     * @return {@link UserDetails} with password hash and authorities
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    logger.debug("User not found during login: {}", username);
                    return new UsernameNotFoundException("Invalid credentials");
                });
    }
}