package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core Spring Security configuration.
 *
 * <p>Configures the security filter chain with JWT-based stateless
 * authentication, Google OAuth2 login, authority mapping, and endpoint
 * authorization rules. Both username/password and OAuth2 authentication
 * converge on the same JWT cookie mechanism.</p>
 *
 * <h4>Stateless architecture (Option 2 — fully cookie-based)</h4>
 * <p>The application is completely stateless. No {@code HttpSession} is
 * ever created or read:</p>
 * <ul>
 *   <li>OAuth2 authorization-code state is stored in a short-lived
 *       {@code oauth2_auth_request} cookie via
 *       {@link HttpCookieOAuth2AuthorizationRequestRepository}.</li>
 *   <li>New-user registration state is stored in an
 *       {@code oauth2_registration} cookie via
 *       {@link com.pkmprojects.shoppiq.auth.oauth2.OAuthRegistrationCookieService}.</li>
 *   <li>Session policy is {@code STATELESS} — Spring Security never
 *       creates or reads a session.</li>
 * </ul>
 *
 * <h4>Request flow (returning user)</h4>
 * <pre>
 * Browser → GET /oauth2/authorization/google
 *       ↓
 * HttpCookieOAuth2AuthorizationRequestRepository.saveAuthorizationRequest()
 *       ↓ (cookie: oauth2_auth_request)
 * Google Login
 *       ↓
 * /login/oauth2/code/google?code=…&state=…
 *       ↓
 * HttpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequest()
 *       ↓ (cookie cleared)
 * OAuth2SuccessHandler — issue JWT cookie, redirect
 *       ↓
 * Every subsequent request — JwtAuthenticationFilter only
 * </pre>
 *
 * @see HttpCookieOAuth2AuthorizationRequestRepository
 * @see OAuth2SuccessHandler
 * @see JwtAuthenticationFilter
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final UserRepository userRepository;
    private final RolesService rolesService;
    private final ShoppiqAuthenticationEntryPoint shoppiqAuthenticationEntryPoint;
    private final ShoppiqAccessDeniedHandler shoppiqAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository,
                          UserRepository userRepository,
                          RolesService rolesService,
                          ShoppiqAuthenticationEntryPoint shoppiqAuthenticationEntryPoint,
                          ShoppiqAccessDeniedHandler shoppiqAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
        this.userRepository = userRepository;
        this.rolesService = rolesService;
        this.shoppiqAuthenticationEntryPoint = shoppiqAuthenticationEntryPoint;
        this.shoppiqAccessDeniedHandler = shoppiqAccessDeniedHandler;
    }

    /**
     * Maps authorities received from Google's OIDC provider into
     * application-specific authorities during the OAuth2 login flow.
     *
     * <p>Returning users receive authorities derived from their database roles.
     * New users receive a temporary CUSTOMER authority that allows access to
     * the registration-completion flow until a local account is created.</p>
     *
     * @return mapper converting OIDC authorities into application authorities
     */
    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    Map<String, Object> userAttributesMap = oidcUserAuthority.getAttributes();
                    String email = (String) userAttributesMap.get("email");

                    if (email != null) {
                        User user = userRepository.findUserByEmail(email).orElse(null);

                        if (user != null) {
                            Set<GrantedAuthority> userAuthorities = user.getRoles().stream()
                                    .filter(role -> {
                                        boolean hasValidName = role.getRoleName() != null && !role.getRoleName().isBlank();
                                        if (!hasValidName) {
                                            logger.warn("User '{}' has role id={} with a null/blank roleName",
                                                    email, role.getId());
                                        }
                                        return hasValidName;
                                    })
                                    .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                                    .collect(Collectors.toSet());
                            mappedAuthorities.addAll(userAuthorities);
                            logger.debug("Mapped returning OAuth2 user '{}' to roles: {}", email, user.getRoles());
                        } else {
                            mappedAuthorities.add(new SimpleGrantedAuthority(rolesService.getCustomerRole().getRoleName()));
                            logger.debug("Mapped new OAuth2 user '{}' to temporary role: CUSTOMER", email);
                        }
                    }
                }
            });

            return mappedAuthorities;
        };
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
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Public frontend
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/allitems",
                                "/complete-profile",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // Authenticated frontend pages
                        .requestMatchers("/address").hasAnyRole("CUSTOMER", "ADMIN")

                        // Public backend
                        .requestMatchers(
                                "/auth/login",
                                "/auth/logout",
                                "/auth/google/get-profile",
                                "/auth/google/complete-profile",
                                "/user/register"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/items/all").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/items/*").hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/items/*/create/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/items/*/reviews").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reviews/*").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/reviews/*/update").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/reviews/*/delete").hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/items/create/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/items/update/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/items/delete/**").hasRole("ADMIN")

                        // Categories: public reads, admin-only writes
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/roles/create/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/roles/all").hasRole("ADMIN")

                        // Cart: customer-only access
                        .requestMatchers("/user/cart/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Address: customer-only access
                        .requestMatchers("/user/address/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Order: customer-only access
                        .requestMatchers("/user/order/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Payment: customer endpoints + admin-only refund
                        .requestMatchers(HttpMethod.PUT, "/user/payment/refund/**").hasRole("ADMIN")
                        .requestMatchers("/user/payment/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Frontend order pages
                        .requestMatchers("/checkout", "/orders", "/order-detail").hasAnyRole("CUSTOMER", "ADMIN")

                        // Frontend payment page
                        .requestMatchers("/payment").hasAnyRole("CUSTOMER", "ADMIN")

                        .anyRequest().authenticated()
                )

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

                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the AuthenticationManager bean for programmatic use in
     * {@link com.pkmprojects.shoppiq.auth.service.AuthService}.
     *
     * @param config Spring Boot auto-configuration
     * @return the AuthenticationManager
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
