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
 * Configures the application's HTTP client infrastructure for outbound calls.
 *
 * <p>This class provides two beans that together form the foundation for all
 * outbound HTTP communication. The {@link InetAddressFilter} bean implements
 * SSRF (Server-Side Request Forgery) protection by rejecting requests that
 * resolve to internal, loopback, or cloud-metadata addresses. The
 * {@link RestClient.Builder} bean is pre-configured with this filter and a
 * 5-second connection timeout.</p>
 *
 * <p>Architecturally, this configuration ensures that all HTTP clients
 * created through the injected builder share the same security posture and
 * timeout behavior. The payment gateway layer, email provider integrations,
 * and any external API clients all rely on this central builder. The SSRF
 * filter uses Spring Boot 4.1's {@link InetAddressFilter#externalAddresses()}
 * to block RFC 1918 private ranges, link-local addresses, loopback
 * interfaces, and cloud metadata endpoints.</p>
 *
 * @author prabhatkrmishra
 * @see PaymentGatewayProperties
 * @since 1.0.0
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates an SSRF protection filter that blocks outbound requests to
     * internal addresses.
     *
     * <p>This filter is applied to all {@link RestClient} instances created
     * by the {@link #restClientBuilder} bean. It uses Spring Boot 4.1's
     * {@link InetAddressFilter#externalAddresses()} which rejects requests
     * targeting loopback addresses (127.0.0.0/8, ::1), RFC 1918 private
     * ranges (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16), link-local
     * addresses (169.254.0.0/16, fe80::/10), cloud metadata endpoints
     * (169.254.169.254), and other non-publicly-routable addresses.</p>
     *
     * @return the SSRF protection {@link InetAddressFilter}
     */
    @Bean
    public InetAddressFilter ssrfProtectionFilter() {
        return InetAddressFilter.externalAddresses();
    }

    /**
     * Creates the primary {@link RestClient.Builder} bean for outbound HTTP calls.
     *
     * <p>This builder is marked as {@code @Primary} so that it is the
     * default injection target when multiple {@link RestClient.Builder}
     * beans exist in the context. It is pre-configured with the SSRF
     * protection filter and a 5-second connection timeout, which prevents
     * the application from hanging indefinitely when an external service
     * is unreachable.</p>
     *
     * <p>The builder uses {@link ClientHttpRequestFactoryBuilder#detect()}
     * to select the best available HTTP client library on the classpath
     * (e.g., Java HttpClient, Apache HttpClient, or Netty). This allows
     * the deployment environment to influence the HTTP stack without code
     * changes.</p>
     *
     * @param ssrfProtectionFilter the filter that blocks internal addresses
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
