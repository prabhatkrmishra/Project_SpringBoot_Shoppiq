package com.pkmprojects.shoppiq.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.http.client.FilteredHostException;
import org.springframework.boot.http.client.InetAddressFilter;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests verifying the SSRF protection configured in {@link RestClientConfig}.
 *
 * <p>Two layers of verification:</p>
 * <ol>
 *   <li><strong>RestClient-level</strong> — builds a {@link RestClient} using the
 *       same {@link InetAddressFilter} as {@link RestClientConfig} and attempts
 *       outbound requests to internal addresses.  The filter intercepts the
 *       connection <em>before</em> TCP, so the request never reaches the
 *       network and throws {@link FilteredHostException} directly.</li>
 *   <li><strong>Filter API-level</strong> — calls
 *       {@link InetAddressFilter#matches(InetAddress)} directly to verify
 *       that public addresses are accepted while every blocked range is
 *       rejected.</li>
 * </ol>
 *
 * <p>WireMock is intentionally <em>not</em> used because it binds to
 * {@code localhost} / {@code 127.0.0.1}, which is always rejected by
 * {@link InetAddressFilter#externalAddresses()}.</p>
 *
 * <h2>Semantics</h2>
 * <p>{@link InetAddressFilter#matches(InetAddress)} returns:</p>
 * <ul>
 *   <li>{@code true} — address is <b>external</b> (allowed through the filter)</li>
 *   <li>{@code false} — address is <b>internal</b> (blocked by the filter)</li>
 * </ul>
 *
 * <h2>Coverage</h2>
 * <ul>
 *   <li>Loopback: {@code 127.0.0.1}, {@code localhost}, {@code ::1}</li>
 *   <li>RFC 1918: {@code 10.x}, {@code 172.16.x}, {@code 192.168.x}</li>
 *   <li>Link-local / cloud-metadata: {@code 169.254.169.254}</li>
 *   <li>"This" network: {@code 0.0.0.0}</li>
 *   <li>Multicast: {@code 224.0.0.1}</li>
 *   <li>IPv6 ULA: {@code fc00::1}, {@code fd00::1}</li>
 *   <li>Public IPv4 accepted by filter: {@code 8.8.8.8}, {@code 1.1.1.1}</li>
 *   <li>Public IPv6 accepted by filter: {@code 2001:4860:4860::8888}</li>
 *   <li>{@link FilteredHostException} carries the blocked host name</li>
 *   <li>{@link RestClientConfig#ssrfProtectionFilter()} produces the
 *       expected filter</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("RestClientConfig SSRF Protection Tests")
class RestClientConfigSsrfTest {

    private RestClient ssrfProtectedClient;
    private InetAddressFilter filter;

    @BeforeAll
    void setUp() {
        filter = new RestClientConfig().ssrfProtectionFilter();

        ssrfProtectedClient = RestClient.builder()
                .requestFactory(
                        org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder.detect()
                                .build(org.springframework.boot.http.client.HttpClientSettings.defaults()
                                        .withInetAddressFilter(filter)))
                .build();
    }

    // ────────────────────────────────────────────────────────────
    // RestClient-level blocking
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Blocks request to 127.0.0.1")
    void blocksLoopbackIp() {
        assertBlocked("http://127.0.0.1:8080/ok", "127.0.0.1");
    }

    @Test
    @DisplayName("Blocks request to localhost")
    void blocksLocalhost() {
        assertBlocked("http://localhost:8080/ok", "localhost");
    }

    @Test
    @DisplayName("Blocks request to 0.0.0.0")
    void blocksAllZeros() {
        assertBlocked("http://0.0.0.0:8080/ok", "0.0.0.0");
    }

    @Test
    @DisplayName("Blocks request to 10.x.x.x (RFC 1918)")
    void blocksPrivateRange10() {
        assertBlocked("http://10.0.0.1:8080/ok", "10.0.0.1");
    }

    @Test
    @DisplayName("Blocks request to 172.16.x.x (RFC 1918)")
    void blocksPrivateRange172() {
        assertBlocked("http://172.16.0.1:8080/ok", "172.16.0.1");
    }

    @Test
    @DisplayName("Blocks request to 192.168.x.x (RFC 1918)")
    void blocksPrivateRange192() {
        assertBlocked("http://192.168.1.1:8080/ok", "192.168.1.1");
    }

    @Test
    @DisplayName("Blocks request to 169.254.169.254 (cloud metadata)")
    void blocksCloudMetadata() {
        assertBlocked("http://169.254.169.254:80/ok", "169.254.169.254");
    }

    @Test
    @DisplayName("Blocks request to 169.254.0.1 (link-local)")
    void blocksLinkLocal() {
        assertBlocked("http://169.254.0.1:80/ok", "169.254.0.1");
    }

    @Test
    @DisplayName("Blocks request to [::1] (IPv6 loopback)")
    void blocksIpv6Loopback() {
        assertBlocked("http://[::1]:8080/ok", "[::1]");
    }

    // ────────────────────────────────────────────────────────────
    // FilteredHostException content
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("FilteredHostException carries the blocked host")
    void filteredHostExceptionContainsHost() {
        assertThatThrownBy(() -> ssrfProtectedClient.get()
                .uri(URI.create("http://127.0.0.1:8080/ok"))
                .retrieve()
                .body(String.class))
                .isInstanceOf(FilteredHostException.class)
                .satisfies(ex -> {
                    FilteredHostException fex = (FilteredHostException) ex;
                    assertThat(fex.getHost()).isEqualTo("127.0.0.1");
                });
    }

    // ────────────────────────────────────────────────────────────
    // Filter API — blocked addresses (matches returns false)
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Filter rejects loopback addresses")
    void filterRejectsLoopback() throws Exception {
        assertThat(filter.matches(InetAddress.getLoopbackAddress())).isFalse();
        assertThat(filter.matches(InetAddress.getByName("::1"))).isFalse();
    }

    @Test
    @DisplayName("Filter rejects RFC 1918 private ranges")
    void filterRejectsRfc1918() throws Exception {
        assertThat(filter.matches(InetAddress.getByName("10.0.0.1"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("10.255.255.255"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("172.16.0.1"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("172.31.255.255"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("192.168.0.1"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("192.168.255.255"))).isFalse();
    }

    @Test
    @DisplayName("Filter rejects link-local and cloud metadata addresses")
    void filterRejectsLinkLocal() throws Exception {
        assertThat(filter.matches(InetAddress.getByName("169.254.169.254"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("169.254.0.1"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("169.254.255.255"))).isFalse();
    }

    @Test
    @DisplayName("Filter rejects 'this' network (0.0.0.0/8)")
    void filterRejectsThisNetwork() throws Exception {
        assertThat(filter.matches(InetAddress.getByName("0.0.0.0"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("0.255.255.255"))).isFalse();
    }

    @Test
    @DisplayName("Filter rejects multicast addresses")
    void filterRejectsMulticast() throws Exception {
        assertThat(filter.matches(InetAddress.getByName("224.0.0.1"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("239.255.255.255"))).isFalse();
    }

    @Test
    @DisplayName("Filter rejects IPv6 ULA addresses")
    void filterRejectsIpv6Ula() throws Exception {
        assertThat(filter.matches(InetAddress.getByName("fc00::1"))).isFalse();
        assertThat(filter.matches(InetAddress.getByName("fd00::1"))).isFalse();
    }

    // ────────────────────────────────────────────────────────────
    // Filter API — allowed public addresses (matches returns true)
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Filter accepts public IPv4 addresses")
    void filterAcceptsPublicIpv4() throws Exception {
        assertThat(filter.matches(InetAddress.getByName("8.8.8.8"))).isTrue();
        assertThat(filter.matches(InetAddress.getByName("1.1.1.1"))).isTrue();
    }

    @Test
    @DisplayName("Filter accepts public IPv6 addresses")
    void filterAcceptsPublicIpv6() throws Exception {
        assertThat(filter.matches(InetAddress.getByName("2001:4860:4860::8888"))).isTrue();
        assertThat(filter.matches(InetAddress.getByName("2606:4700:4700::1111"))).isTrue();
    }

    // ────────────────────────────────────────────────────────────
    // RestClientConfig bean verification
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ssrfProtectionFilter bean blocks internal and allows external")
    void filterBeanIsCorrectType() throws Exception {
        InetAddressFilter bean = new RestClientConfig().ssrfProtectionFilter();

        assertThat(bean).isNotNull();
        // Public addresses allowed
        assertThat(bean.matches(InetAddress.getByName("8.8.8.8"))).isTrue();
        // Private addresses blocked
        assertThat(bean.matches(InetAddress.getByName("127.0.0.1"))).isFalse();
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    /**
     * Assert that a request to the given URI is blocked by the SSRF filter,
     * throwing a {@link FilteredHostException} containing the expected host.
     *
     * <p>In Spring Boot 4.1, {@link FilteredHostException} is thrown directly
     * (not wrapped in {@link org.springframework.web.client.ResourceAccessException})
     * because the filter intercepts the connection before TCP is established.</p>
     */
    private void assertBlocked(String uri, String expectedHost) {
        assertThatThrownBy(() -> ssrfProtectedClient.get()
                .uri(URI.create(uri))
                .retrieve()
                .body(String.class))
                .isInstanceOf(FilteredHostException.class)
                .satisfies(ex -> {
                    FilteredHostException fex = (FilteredHostException) ex;
                    assertThat(fex.getHost()).isEqualTo(expectedHost);
                });
    }
}
