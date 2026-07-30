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

import com.pkmprojects.shoppiq.config.CheckoutProperties;
import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.aiservice.service.ChatServiceImpl;

import java.util.Arrays;

/**
 * Main entry point for the Shoppiq e-commerce application.
 *
 * <p>Bootstraps the Spring Boot application with async task execution and
 * scheduled job support enabled. The {@code @EnableScheduling} annotation
 * powers the auto-resolve scheduler in {@link ChatServiceImpl}, while
 * {@code @EnableAsync} allows non-blocking background task execution
 * throughout the application.</p>
 *
 * <p>Registers {@link PaginationProperties}, {@link PaymentGatewayProperties},
 * and {@link CheckoutProperties} as configuration property bindings for
 * centralized defaults. On startup, the application logs active profiles
 * and AI-service configuration status ({@code shoppiq.ai.enabled}) for
 * operational diagnostics.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({PaginationProperties.class, PaymentGatewayProperties.class, CheckoutProperties.class})
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
