package com.pkmprojects.shoppiq.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.InetAddressFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * <strong>Spring Boot Concept:</strong> {@code @Configuration} class that
 * configures the application's HTTP client infrastructure.
 *
 * <p>Provides a primary {@link RestClient.Builder} bean pre-configured with
 * an {@link InetAddressFilter} for SSRF (Server-Side Request Forgery)
 * protection and a 5-second connection timeout. The filter uses Spring
 * Boot 4.1's {@link InetAddressFilter#externalAddresses()} which blocks
 * loopback, RFC 1918 private ranges, link-local addresses, cloud metadata
 * endpoints, and other non-publicly-routable addresses.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates an SSRF protection filter that blocks outbound requests to
     * internal addresses.
     *
     * <p>Uses Spring Boot 4.1's {@link InetAddressFilter#externalAddresses()}
     * to deny requests to loopback, RFC 1918 private ranges, link-local,
     * cloud metadata, and other non-publicly-routable addresses.</p>
     *
     * @return the SSRF protection filter
     */
    @Bean
    public InetAddressFilter ssrfProtectionFilter() {
        return InetAddressFilter.externalAddresses();
    }

    /**
     * Creates the primary {@link RestClient.Builder} bean for outbound HTTP calls.
     *
     * <p>Pre-configured with SSRF protection and a 5-second connection timeout.
     * Used by the payment gateway layer for outbound API calls to external
     * providers.</p>
     *
     * @param ssrfProtectionFilter the SSRF protection filter
     * @return a configured {@link RestClient.Builder} instance
     */
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder(InetAddressFilter ssrfProtectionFilter) {
        HttpClientSettings settings = HttpClientSettings.defaults()
                .withInetAddressFilter(ssrfProtectionFilter)
                .withConnectTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings));
    }
}
