package com.pkmprojects.shoppiq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configures the application's password encoding strategy.
 *
 * <p>This class exposes a single {@link PasswordEncoder} bean backed by
 * {@link BCryptPasswordEncoder}. BCrypt is the industry-standard choice
 * for password hashing because it includes a per-hash salt, is adaptive
 * (cost factor can be increased over time), and is resistant to rainbow
 * table and timing attacks. The default strength of 10 rounds provides a
 * good balance between security and performance for most web applications.</p>
 *
 * <p>Architecturally, this bean is injected wherever password hashing or
 * verification is required: the {@code DaoAuthenticationProvider} uses it
 * during login to verify submitted passwords against stored hashes, and
 * registration or password-change services use it to hash new passwords
 * before persisting them. Having a single shared encoder ensures that
 * all password operations use the same algorithm and cost factor.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
public class PasswordEncoderConfig {
    /**
     * Provides a BCrypt password encoder for hashing and verifying passwords.
     *
     * <p>The encoder is used automatically by Spring Security's
     * {@code DaoAuthenticationProvider} when validating credentials during
     * the login flow. The same bean must be injected wherever passwords are
     * hashed at registration time or during password-change operations. The
     * default cost factor of 10 rounds produces a hash that takes
     * approximately 100ms on modern hardware, making brute-force attacks
     * computationally expensive.</p>
     *
     * @return a {@link BCryptPasswordEncoder} with the default strength of 10 rounds
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
