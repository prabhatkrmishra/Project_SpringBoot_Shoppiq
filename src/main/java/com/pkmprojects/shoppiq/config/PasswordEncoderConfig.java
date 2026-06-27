package com.pkmprojects.shoppiq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
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
