package com.pkmprojects.shoppiq.email.provider;

import com.pkmprojects.shoppiq.exception.general.email.EmailProviderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry that collects all {@link EmailProvider} beans and resolves the active one based on configuration.
 *
 * <p>Indexes providers by name at startup and provides O(1) lookup for the active provider.
 * The registry collects all enabled EmailProvider beans, indexes them by their provider name,
 * and resolves the active provider based on the {@code shoppiq.email.provider} configuration
 * property. If the configured provider is not available, it falls back to any available provider.</p>
 *
 * <p>This registry is used by the EmailServiceImpl to obtain the active email provider for
 * sending emails. The registry pattern ensures that email provider resolution happens once
 * at startup, avoiding repeated bean lookups during email delivery.</p>
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
            throw EmailProviderNotFoundException.byName(providerName, providers.keySet());
        }
        return provider;
    }

    private EmailProvider resolveProvider(String activeProvider) {
        EmailProvider provider = providers.get(activeProvider.toUpperCase(Locale.ROOT));
        if (provider == null) {
            log.warn("Configured email provider '{}' not found, falling back to any available provider",
                    activeProvider);
            provider = providers.values().stream().findFirst()
                    .orElseThrow(EmailProviderNotFoundException::noneAvailable);
        }
        return provider;
    }
}
