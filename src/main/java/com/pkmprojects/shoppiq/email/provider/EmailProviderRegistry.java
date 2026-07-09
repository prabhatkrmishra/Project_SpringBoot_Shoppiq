package com.pkmprojects.shoppiq.email.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the correct {@link EmailProvider} based on configuration.
 *
 * <p>
 * All {@link EmailProvider} beans are collected at startup and indexed
 * by their provider name. The active provider is determined by the
 * {@code shoppiq.email.provider} configuration property.
 * </p>
 *
 * @author PrabhatKrMishra
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
        EmailProvider provider = providers.get(providerName.toUpperCase());
        if (provider == null) {
            throw new IllegalStateException("No email provider found for: " + providerName
                    + ". Available: " + providers.keySet());
        }
        return provider;
    }

    private EmailProvider resolveProvider(String activeProvider) {
        EmailProvider provider = providers.get(activeProvider.toUpperCase());
        if (provider == null) {
            log.warn("Configured email provider '{}' not found, falling back to any available provider",
                    activeProvider);
            provider = providers.values().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No email providers available"));
        }
        return provider;
    }
}
