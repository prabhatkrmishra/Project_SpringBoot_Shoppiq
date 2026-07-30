package com.pkmprojects.shoppiq.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.cfg.DateTimeFeature;

import java.text.SimpleDateFormat;

/**
 * Configures global Jackson 3 serialization and deserialization settings.
 *
 * <p>This class registers a {@link JsonMapperBuilderCustomizer} bean that
 * applies uniform serialization rules across the entire application. The
 * customizer enforces lowerCamelCase property naming, ISO-8601 date-time
 * formatting, and disables the legacy practice of writing dates as numeric
 * timestamps. These settings ensure consistent JSON output for all REST
 * API responses and request deserialization.</p>
 *
 * <p>Architecturally, this configuration acts as the single point of
 * control for JSON behavior. By centralizing the customizer here, every
 * {@code ObjectMapper} created by Spring Boot (including those used by
 * Spring MVC, Spring Data REST, and WebClient) inherits the same rules.
 * This prevents subtle serialization mismatches between controllers,
 * repositories, and external integrations.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
public class JacksonConfig {

    /**
     * The standard ISO date-time format pattern with millisecond precision.
     *
     * <p>Produces output such as {@code "2026-07-28T11:42:00.000+05:30"},
     * which includes the full date, time, milliseconds, and timezone offset.
     * This format is used as the default date serialization pattern when
     * no explicit {@code @JsonFormat} annotation is present on a field.</p>
     */
    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * Creates a customizer bean for global Jackson 3 mapper configuration.
     *
     * <p>This customizer applies three critical settings to every
     * {@code ObjectMapper} in the application context. First, it enforces
     * lowerCamelCase property naming via {@link PropertyNamingStrategies}
     * to guarantee consistent field names in JSON payloads regardless of
     * Java naming conventions. Second, it disables
     * {@link DateTimeFeature#WRITE_DATES_AS_TIMESTAMPS} so that dates are
     * serialized as ISO-8601 strings rather than numeric epoch values.
     * Third, it registers a {@link SimpleDateFormat} with the ISO pattern
     * as the fallback date formatter for any date type that does not carry
     * an explicit format annotation.</p>
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
