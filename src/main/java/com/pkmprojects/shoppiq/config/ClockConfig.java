package com.pkmprojects.shoppiq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a system UTC {@link Clock} bean for deterministic time handling.
 *
 * <p>This configuration exposes a {@link Clock} bean backed by
 * {@link Clock#systemUTC()}. Injecting {@link Clock} instead of calling
 * {@code Instant.now()} or {@code LocalDateTime.now()} directly enables
 * deterministic testing by allowing tests to override the bean with a
 * fixed or mocked clock. This is critical for time-sensitive logic such
 * as token expiration, rate-limit window calculations, and audit
 * timestamp generation.</p>
 *
 * <p>Architecturally, this bean acts as the single source of time for the
 * entire application. All components that need the current time should
 * inject this {@link Clock} rather than using system time directly. This
 * ensures that time-dependent behavior can be tested without threading
 * or sleeping, and that clock adjustments (e.g., NTP sync) are
 * consistently reflected across all components.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
public class ClockConfig {

    /**
     * Creates a system UTC clock bean.
     *
     * <p>Returns {@link Clock#systemUTC()} which provides the current
     * instant with millisecond precision. In tests, this bean can be
     * overridden with {@code Clock.fixed(...)} or {@code Clock.offset(...)}
     * to control time progression deterministically.</p>
     *
     * @return a configured {@link Clock} instance using system UTC time
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
