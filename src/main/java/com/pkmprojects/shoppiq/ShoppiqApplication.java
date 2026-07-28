package com.pkmprojects.shoppiq;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.aiservice.service.ChatServiceImpl;

import java.util.Arrays;

/**
 * <strong>Spring Boot Concept:</strong> Main entry point for the Shoppiq e-commerce application.
 *
 * <p><b>How AI fits:</b> The application bootstraps with {@code @EnableScheduling}
 * which powers the auto-resolve scheduler in {@link ChatServiceImpl},
 * and logs the AI-service configuration status ({@code shoppiq.ai.enabled})
 * on startup for operational diagnostics.</p>
 *
 * <p>Bootstraps the Spring Boot application with async task execution and
 * scheduled job support enabled. Registers {@link PaginationProperties}
 * as a configuration property binding for centralized pagination defaults.</p>
 *
 * <p>On startup, the application logs active profiles and AI-service
 * configuration status for operational diagnostics.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({PaginationProperties.class, PaymentGatewayProperties.class})
public class ShoppiqApplication {

    private static final Logger log = LoggerFactory.getLogger(ShoppiqApplication.class);

    private final Environment env;

    public ShoppiqApplication(Environment env) {
        this.env = env;
    }

    @PostConstruct
    void logStartup() {
        String[] active = env.getActiveProfiles();
        boolean aiEnabled = "true".equals(env.getProperty("shoppiq.ai.enabled"));
        log.debug("[AI-STARTUP] Active profiles: {} — ai-enabled={}", Arrays.toString(active), aiEnabled);
        if (aiEnabled) {
            log.debug("[AI-STARTUP] AI_NVIDIA_API_KEY present: {}",
                    env.getProperty("AI_NVIDIA_API_KEY") != null ? "yes (len=" + env.getProperty("AI_NVIDIA_API_KEY").length() + ")" : "NO");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ShoppiqApplication.class, args);
    }

}
