package com.pkmprojects.shoppiq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Configuration;

/**
 * <strong>Spring Boot Concept:</strong> {@code @Configuration} class that
 * configures the application's password encoding strategy.
 *
 * <p>Exposes a {@link PasswordEncoder} bean backed by
 * {@link BCryptPasswordEncoder} (default strength of 10 rounds). Used by
 * Spring Security's {@code DaoAuthenticationProvider} for credential
 * validation during login and by service classes for password hashing
 * during registration.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
public class PasswordEncoderConfig {
    /**
     * Provides a BCrypt password encoder for hashing and verifying passwords.
     *
     * <p>Used automatically by {@code DaoAuthenticationProvider} when
     * validating credentials during login. The same bean should be injected
     * wherever passwords are hashed at registration time.</p>
     *
     * @return a {@link BCryptPasswordEncoder} with default strength (10 rounds)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
