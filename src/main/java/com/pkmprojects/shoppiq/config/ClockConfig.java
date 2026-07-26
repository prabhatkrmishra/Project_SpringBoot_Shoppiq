package com.pkmprojects.shoppiq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a {@link Clock} bean used throughout the application for
 * deterministic time in business logic. Defaults to system UTC clock;
 * tests can override with a fixed or mocked clock.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
