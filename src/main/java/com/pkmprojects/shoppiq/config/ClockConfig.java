package com.pkmprojects.shoppiq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * <strong>Spring Boot Concept:</strong> {@code @Configuration} class that
 * provides a {@link Clock} bean for deterministic time in business logic.
 *
 * <p>Defaults to {@code Clock.systemUTC()}; tests can override with a
 * fixed or mocked clock. Used wherever time-based logic needs to be
 * testable — including AI chat conversation timestamps and auto-resolve
 * cutoff logic.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
public class ClockConfig {

    /**
     * Creates a system UTC clock bean.
     *
     * @return a configured {@link Clock} instance
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
