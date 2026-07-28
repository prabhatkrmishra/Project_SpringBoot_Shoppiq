package com.pkmprojects.shoppiq.email.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <strong>Spring Boot Concept:</strong> Registry that collects all
 * {@link EmailProvider} beans at startup and resolves the active one based
 * on external configuration. Analogous to {@link com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayRegistry}.
 *
 * <p>
 * All {@link EmailProvider} beans are collected at startup and indexed
 * by their provider name (via {@link EmailProvider#getProviderName()}).
 * The active provider is determined by the {@code shoppiq.email.provider}
 * configuration property.
 * </p>
 *
 * <p><strong>Educational value:</strong> This class demonstrates Spring's
 * <strong>Bean aggregation + Registry</strong> pattern:
 * <ul>
 *   <li>Spring injects all beans implementing {@code EmailProvider} into the
 *       constructor as a {@code List}.</li>
 *   <li>The registry filters by {@code isEnabled()} and indexes them by name
 *       into a {@link java.util.Map} for O(1) lookup.</li>
 *   <li>The active provider is resolved once at construction time based on
 *       the {@code @Value} configuration property, with a fallback to any
 *       available provider if the configured one is missing.</li>
 *   <li>This is the same pattern used in the payment gateway subsystem
 *       ({@link PaymentGatewayRegistry}) — the application uses the same
 *       architectural approach for both email and payment providers,
 *       making the codebase consistent and predictable.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Component
public class EmailProviderRegistry {

    private final Map<String, EmailProvider> providers;
    private final EmailProvider defaultProvider;

    public EmailProviderRegistry(List<EmailProvider> providerList,
                                  @Value("${shoppiq.email.provider:console}") String activeProvider) {
        this.providers = providerList.stream()
                .filter(EmailProvider::isEnabled)
                .collect(Collectors.toMap(
                        EmailProvider::getProviderName,
                        Function.identity()
                ));

        this.defaultProvider = resolveProvider(activeProvider);

        log.info("Email provider registry initialized. Active provider: {}, Available: {}",
                activeProvider, providers.keySet());
    }

    /**
     * Returns the currently active email provider.
     *
     * @return active email provider
     */
    public EmailProvider getActiveProvider() {
        return defaultProvider;
    }

    /**
     * Resolves a provider by name.
     *
     * @param providerName the provider name
     * @return the matching provider
     * @throws IllegalStateException if no provider matches
     */
    public EmailProvider resolve(String providerName) {
        EmailProvider provider = providers.get(providerName.toUpperCase(Locale.ROOT));
        if (provider == null) {
            throw new IllegalStateException("No email provider found for: " + providerName
                    + ". Available: " + providers.keySet());
        }
        return provider;
    }

    private EmailProvider resolveProvider(String activeProvider) {
        EmailProvider provider = providers.get(activeProvider.toUpperCase(Locale.ROOT));
        if (provider == null) {
            log.warn("Configured email provider '{}' not found, falling back to any available provider",
                    activeProvider);
            provider = providers.values().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No email providers available"));
        }
        return provider;
    }
}
