package com.pkmprojects.shoppiq.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.cfg.DateTimeFeature;

import java.text.SimpleDateFormat;

/**
 * <strong>Spring Boot Concept:</strong> {@code @Configuration} class that
 * configures global Jackson 3 serialization settings.
 *
 * <p>Provides a {@link JsonMapperBuilderCustomizer} bean to extend and
 * fine-tune Spring Boot 4's autoconfigured Jackson 3 environment for
 * framework-level serialization (HTTP message converters, reactive codecs,
 * RFC 9457 {@code ProblemDetail} formatting).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
public class JacksonConfig {

    /**
     * The standard ISO date-time format pattern with millisecond precision and timezone offset.
     * <p>
     * Example output format: {@code "2026-07-28T11:42:00.000+05:30"}
     * </p>
     */
    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * Creates a customizer bean for global Jackson 3 mapper configuration.
     *
     * <p>Configures lowerCamelCase property naming, ISO date-time format
     * ({@code yyyy-MM-dd'T'HH:mm:ss.SSSXXX}), and disables writing dates
     * as timestamps.</p>
     *
     * @return a configured {@link JsonMapperBuilderCustomizer} instance
     */
    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                /*
                 * Force globally uniform lowerCamelCase mapping using Jackson 3 naming strategies
                 */
                .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)

                /*
                 * Ensure modern date features are natively active (e.g. text instead of raw arrays)
                 */
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)

                /*
                 * Register the explicit ISO pattern fallback engine using a clone-safe formatter instance
                 */
                .defaultDateFormat(new SimpleDateFormat(DATETIME_FORMAT));
    }
}
