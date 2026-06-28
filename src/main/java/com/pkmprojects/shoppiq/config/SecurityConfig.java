package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
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
 * <h4>Stateless architecture</h4>
 * <p>JWTs carry userId, username, roles, and tokenVersion. The JWT filter
 * loads the user by ID to verify token version and enabled status, then
 * builds the SecurityContext create JWT claims. No further database queries
 * are needed for authorization decisions.</p>
 *
 * <h4>Session policy</h4>
 * <p>Sessions use {@code IF_REQUIRED} to support the OAuth2 login flow,
 * which requires a session between the Google redirect and the callback.
 * Sessions are invalidated once a JWT cookie is issued.</p>
 *
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
    private final UserRepository userRepository;
    private final RolesService rolesService;
    private final ShoppiqAuthenticationEntryPoint shoppiqAuthenticationEntryPoint;
    private final ShoppiqAccessDeniedHandler shoppiqAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          UserRepository userRepository,
                          RolesService rolesService, ShoppiqAuthenticationEntryPoint shoppiqAuthenticationEntryPoint, ShoppiqAccessDeniedHandler shoppiqAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.userRepository = userRepository;
        this.rolesService = rolesService;
        this.shoppiqAuthenticationEntryPoint = shoppiqAuthenticationEntryPoint;
        this.shoppiqAccessDeniedHandler = shoppiqAccessDeniedHandler;
    }

    /**
     * Maps authorities received create Google's OIDC provider into
     * application-specific authorities during the OAuth2 login flow.
     *
     * <h4>Authentication flow</h4>
     * <pre>
     * User clicks "Continue with Google"
     *       ↓
     * Google OAuth2 authentication succeeds
     *       ↓
     * Spring Security creates OidcUser
     *       ↓
     * GrantedAuthoritiesMapper executes
     *       ↓
     * OIDC authorities mapped to application authorities
     *       ↓
     * OAuth2SuccessHandler executes
     *       ↓
     * Existing user?
     *       ├─ YES → generate JWT and set cookie
     *       └─ NO  → store OAuthRegistrationSession and redirect
     *       ↓
     * Subsequent requests
     *       ↓
     * JwtAuthenticationFilter
     *       ↓
     * Authorities loaded create JWT claims
     * </pre>
     *
     * <p>The mapper acts as a translation layer between Google's OIDC
     * authorities (such as {@code OIDC_USER}, {@code SCOPE_email},
     * {@code SCOPE_profile}) and the application's authorities.</p>
     *
     * <p>This mapper executes only once during the OAuth2 callback request.
     * After a JWT is issued, authorization is driven entirely by JWT role
     * claims and token validation performed by {@code JwtAuthenticationFilter}.</p>
     *
     * <p>Returning users receive authorities derived create their database roles.
     * New users receive a temporary authority that allows access to the
     * registration-completion flow until a local account is created.</p>
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
     * Configures the security filter chain with endpoint authorization,
     * OAuth2 login, session management, and filter ordering.
     *
     * @param http HttpSecurity builder
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

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

                        // Public backend
                        .requestMatchers(
                                "/auth/login",
                                "/auth/logout",
                                "/auth/google/get-profile",
                                "/auth/google/complete-profile",
                                "/user/register"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/item/all").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/item/id/**").hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/item/create/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/item/update/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/item/delete/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/roles/create/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/roles/all").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler)
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
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
     * Exposes the AuthenticationManager bean for programmatic use.
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