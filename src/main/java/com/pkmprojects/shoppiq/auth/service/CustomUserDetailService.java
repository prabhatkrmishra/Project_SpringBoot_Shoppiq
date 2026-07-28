package com.pkmprojects.shoppiq.auth.service;import com.pkmprojects.shoppiq.auth.security.SecurityUser;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService} —
 * the bridge between the database and the authentication provider.
 *
 * <h3>Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>UserDetailsService contract</strong> — the core SPI that
 *       {@code DaoAuthenticationProvider} calls during password-based login.
 *       It loads a user by username and returns a {@link UserDetails} object
 *       that the provider uses to verify the password and check account status.</li>
 *   <li><strong>DaoAuthenticationProvider flow</strong> — Spring Security's
 *       default authentication provider: (1) calls {@code loadUserByUsername()}
 *       → (2) compares the submitted password with the stored hash via
 *       {@code PasswordEncoder} → (3) checks {@code isEnabled()},
 *       {@code isAccountNonLocked()}, etc.</li>
 *   <li><strong>Dual authentication paths</strong> — this service is <em>only</em>
 *       used for password-based login (via {@code AuthenticationManager}). The
 *       JWT authentication filter ({@link com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter})
 *       loads the user directly by ID, bypassing this service entirely.</li>
 *   <li><strong>UsernameNotFoundException</strong> — Spring Security requires
 *       this exception (not a generic one) for the provider to properly
 *       handle "user not found" as a failed authentication (bad credentials)
 *       rather than a system error.</li>
 * </ul>
 *
 * <h3>Authentication flow (password login)</h3>
 * <pre>
 * POST /auth/login
 *       ↓
 * AuthService creates UsernamePasswordAuthenticationToken(username, password)
 *       ↓
 * AuthenticationManager.authenticate() → DaoAuthenticationProvider
 *       ↓
 * DaoAuthenticationProvider calls loadUserByUsername(username)
 *       ↓
 * CustomUserDetailService queries database → returns SecurityUser
 *       ↓
 * DaoAuthenticationProvider verifies password via PasswordEncoder
 *       ↓
 * Authentication succeeds → AuthService generates JWT cookie
 * </pre>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Service pattern</strong> — implements the Spring Security SPI
 *       to provide application-specific user lookup logic.</li>
 *   <li><strong>Adapter pattern</strong> — returns a {@link SecurityUser}
 *       wrapper that adapts the JPA entity ({@link User}) to Spring Security's
 *       {@link UserDetails} interface.</li>
 *   <li><strong>Information hiding</strong> — the error message is deliberately
 *       generic ("Invalid credentials") regardless of whether the username
 *       exists, preventing username enumeration attacks.</li>
 * </ul>
 *
 * @see SecurityUser
 * @see com.pkmprojects.shoppiq.auth.service.AuthService
 *
 * @author prabhatkrmishra
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
