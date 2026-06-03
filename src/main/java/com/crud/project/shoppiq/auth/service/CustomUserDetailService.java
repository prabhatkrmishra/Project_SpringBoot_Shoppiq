package com.crud.project.shoppiq.auth.service;

import com.crud.project.shoppiq.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * Bridges the application's {@code UserRepository} with Spring Security's
 * authentication infrastructure.
 *
 * <p>Called in two distinct contexts:</p>
 * <ol>
 *   <li><b>Login</b> — invoked by {@code DaoAuthenticationProvider} inside
 *       {@code AuthenticationManager.authenticate()} to load the user whose
 *       credentials are being verified.</li>
 *   <li><b>Per-request JWT validation</b> — invoked by
 *       {@code JwtAuthenticationFilter} after extracting a valid username from
 *       the JWT cookie, to load full user details and populate the
 *       {@code SecurityContext}.</li>
 * </ol>
 */
@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username from the database.
     *
     * <p>Returns a {@link UserDetails} object that Spring Security uses to
     * verify the password (during login) and to resolve roles and authorities
     * (on every authenticated request).</p>
     *
     * @param username the username extracted from the login request or JWT cookie
     * @return {@link UserDetails} for the matching user
     * @throws UsernameNotFoundException if no user exists with the given username,
     *                                   or if a database error occurs during lookup
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userRepository.findUserByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } catch (Exception e) {
            throw new UsernameNotFoundException("Database error while loading user", e);
        }
    }
}