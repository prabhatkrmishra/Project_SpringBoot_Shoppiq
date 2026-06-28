package com.pkmprojects.shoppiq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

/**
 * Jackson configuration for the Shoppiq application.
 *
 * <p>
 * This configuration provides the application's primary
 * {@link ObjectMapper} instance used for JSON serialization and
 * deserialization throughout the application.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Provide a singleton {@link ObjectMapper} bean.</li>
 *     <li>Serve as the central location for future Jackson customization.</li>
 *     <li>Ensure consistent JSON serialization across the application.</li>
 * </ul>
 *
 * <h2>Current Configuration</h2>
 * <ul>
 *     <li>Registers the application's primary {@link ObjectMapper}.</li>
 *     <li>Registers {@link ProblemDetailJacksonMixin} so RFC 9457 extension
 *     properties (e.g. {@code errorCode}, {@code timestamp}) flatten to the
 *     top level of the JSON response instead of nesting under a
 *     {@code properties} object.</li>
 * </ul>
 *
 * <h2>Why this mixin must be registered explicitly</h2>
 * <p>
 * {@code Jackson2ObjectMapperBuilder} (used by Spring Boot's own
 * autoconfigured {@code ObjectMapper}) registers {@link ProblemDetailJacksonMixin}
 * automatically. This bean builds its {@link ObjectMapper} manually
 * (<code>new ObjectMapper()</code>) to keep full control over serialization,
 * which bypasses that auto-registration entirely. Without this mixin,
 * {@code GlobalExceptionHandler}/{@code ProblemDetailResponseWriter} responses
 * would still set {@code errorCode} via {@code ProblemDetail.setProperty(...)},
 * but it would serialize nested under a {@code "properties"} object rather
 * than at the top level — breaking the RFC 9457 contract this application
 * is built around.
 * </p>
 *
 * <h2>Future Scope</h2>
 * <ul>
 *     <li>Register custom Jackson modules.</li>
 *     <li>Configure serialization and deserialization features.</li>
 *     <li>Add custom serializers and deserializers.</li>
 *     <li>Configure property naming strategies.</li>
 *     <li>Configure JSON inclusion policies.</li>
 *     <li>Support application-wide JSON customization.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>This class intentionally centralizes Jackson configuration.</li>
 *     <li>All future JSON-related configuration should be added here.</li>
 *     <li>Only one {@link ObjectMapper} bean should exist within the application context.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates the application's primary {@link ObjectMapper}.
     *
     * <p>
     * The returned mapper is managed as a singleton Spring bean and is
     * injected wherever an {@link ObjectMapper} dependency is required,
     * including REST controllers, Spring Security components, and
     * application services.
     * </p>
     *
     * @return the application's primary {@link ObjectMapper}
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addMixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class);
    }
}