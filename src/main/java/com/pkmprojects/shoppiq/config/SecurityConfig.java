package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.oauth2.OAuthReturnUrlFilter;
import com.pkmprojects.shoppiq.filter.RateLimitFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> {@code @Configuration} class that
 * configures the core Spring Security filter chain.
 *
 * <p>Provides JWT-based stateless authentication, Google OAuth2 login,
 * authority mapping, and endpoint authorization rules. The application is
 * completely stateless — no {@code HttpSession} is ever created or read.
 * OAuth2 authorization-code state is stored in short-lived cookies via
 * {@link HttpCookieOAuth2AuthorizationRequestRepository}. Both username/
 * password and OAuth2 authentication converge on the same JWT cookie
 * mechanism.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final ShoppiqAuthenticationEntryPoint shoppiqAuthenticationEntryPoint;
    private final ShoppiqAccessDeniedHandler shoppiqAccessDeniedHandler;
    private final OAuthReturnUrlFilter oauthReturnUrlFilter;
    private final Optional<RateLimitFilter> rateLimitFilter;
    private final Optional<CorsConfigurationSource> corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository,
                          ShoppiqAuthenticationEntryPoint shoppiqAuthenticationEntryPoint,
                          ShoppiqAccessDeniedHandler shoppiqAccessDeniedHandler,
                          OAuthReturnUrlFilter oauthReturnUrlFilter,
                          Optional<RateLimitFilter> rateLimitFilter,
                          Optional<CorsConfigurationSource> corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
        this.shoppiqAuthenticationEntryPoint = shoppiqAuthenticationEntryPoint;
        this.shoppiqAccessDeniedHandler = shoppiqAccessDeniedHandler;
        this.oauthReturnUrlFilter = oauthReturnUrlFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Configures the fully stateless security filter chain.
     *
     * <h4>Session policy</h4>
     * <p>{@code STATELESS} — Spring Security never creates or consults an
     * {@code HttpSession}. OAuth2 state is carried in cookies exclusively.</p>
     *
     * <h4>OAuth2 authorization request repository</h4>
     * <p>{@link HttpCookieOAuth2AuthorizationRequestRepository} is wired into
     * both {@code authorizationEndpoint()} and {@code redirectionEndpoint()},
     * replacing the default {@code HttpSessionOAuth2AuthorizationRequestRepository}.</p>
     *
     * @param http HttpSecurity builder
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> corsConfigurationSource.ifPresent(cors::configurationSource))

                .csrf(csrf -> csrf
                        .spa()
                        .ignoringRequestMatchers(
                                "/auth/login",
                                "/auth/logout",
                                "/auth/refresh",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/auth/google/**",
                                "/user/register",
                                "/api/newsletter/**",
                                "/api/banners/active",
                                "/api/ai/guest/**",
                                "/api/ai/chat",
                                "/api/admin/test/items/bulk",
                                "/api/admin/test/categories/bulk",
                                "/api/admin/test/users/bulk",
                                "/api/admin/test/addresses/bulk",
                                "/api/admin/test/reviews/bulk",
                                "/api/admin/test/sellers/bulk",
                                "/api/admin/test/carts/bulk",
                                "/api/admin/test/orders/bulk"
                        )
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public frontend
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/forgot-password",
                                "/reset-password",
                                "/allitems",
                                "/new-arrivals",
                                "/sale",
                                "/contact",
                                "/categories",
                                "/category/**",
                                "/item/**",
                                "/complete-profile",
                                "/terms",
                                "/privacy",
                                "/about",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // Customer-only: cart, Address: customer + seller, Profile: customer + seller + admin
                        .requestMatchers("/cart").hasRole("CUSTOMER")
                        .requestMatchers("/address").hasAnyRole("CUSTOMER", "SELLER")
                        .requestMatchers("/profile").hasAnyRole("CUSTOMER", "SELLER", "ADMIN")

                        // Admin frontend pages
                        .requestMatchers(
                                "/admin/dashboard",
                                "/admin/inventory",
                                "/admin/orders",
                                "/admin/users",
                                "/admin/payments",
                                "/admin/reviews",
                                "/admin/reports",
                                "/admin/categories",
                                "/admin/categories/import",
                                "/admin/sellers",
                                "/admin/pending",
                                "/admin/products",
                                "/admin/roles",
                                "/admin/promo-codes",
                                "/admin/mail",
                                "/admin/messages",
                                "/admin/banners"
                        ).hasRole("ADMIN")

                        // Admin API endpoints
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // Public backend
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/google/get-profile").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/google/complete-profile").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/verify-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/confirm-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/contact").permitAll()

                        // Newsletter: public subscribe + unsubscribe
                        .requestMatchers(HttpMethod.POST, "/api/newsletter/subscribe").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/newsletter/unsubscribe").permitAll()

                        // Banners: public read
                        .requestMatchers(HttpMethod.GET, "/api/banners/active").permitAll()

                        .requestMatchers(HttpMethod.GET, "/items/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/items/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/items/slug/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/items/*/create/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/items/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reviews/*").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/reviews/*/update").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/reviews/*/delete").hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/items/create/**").hasRole("ADMIN")

                        // Categories: public reads, admin-only writes
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/roles/create/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/roles/all").hasRole("ADMIN")

                        // Seller: profile view + registration for any authenticated user, management requires SELLER role
                        .requestMatchers(HttpMethod.GET, "/seller/profile").authenticated()
                        .requestMatchers(HttpMethod.POST, "/seller/register").authenticated()
                        .requestMatchers("/seller/**").hasAnyRole("SELLER", "ADMIN")

                        // Cart: customer & admin access
                        .requestMatchers("/user/cart/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Address: customer & seller, admin access
                        .requestMatchers("/user/address/**").hasAnyRole("CUSTOMER", "SELLER", "ADMIN")

                        // Order: customer & admin access
                        .requestMatchers("/user/order/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Payment: customer & admin access
                        .requestMatchers("/user/payment/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Frontend order pages
                        .requestMatchers("/checkout", "/orders", "/order-detail").hasAnyRole("CUSTOMER", "ADMIN")

                        // Frontend payment page
                        .requestMatchers("/payment").hasAnyRole("CUSTOMER", "ADMIN")

                        // AI chat
                        .requestMatchers(HttpMethod.POST, "/api/ai/guest/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ai/guest/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/ai/guest/**").permitAll()
                        .requestMatchers("/api/ai/**").hasAnyRole("CUSTOMER", "ADMIN")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form.disable())

                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(shoppiqAuthenticationEntryPoint)
                        .accessDeniedHandler(shoppiqAccessDeniedHandler)
                )

                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)  // 1 year
                        )
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; base-uri 'self'; form-action 'self'; frame-ancestors 'self'; connect-src 'self' https://unpkg.com https://cdn.jsdelivr.net https://cdn.quilljs.com; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://unpkg.com https://cdn.quilljs.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.quilljs.com https://cdn.jsdelivr.net; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https:")
                        )
                )

                .addFilterBefore(oauthReturnUrlFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        rateLimitFilter.ifPresent(filter ->
                http.addFilterBefore(filter, JwtAuthenticationFilter.class));

        return http.build();
    }

    /**
     * Exposes the {@link AuthenticationManager} bean for programmatic use
     * in authentication services.
     *
     * @param config Spring Boot auto-configuration
     * @return the {@link AuthenticationManager}
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
