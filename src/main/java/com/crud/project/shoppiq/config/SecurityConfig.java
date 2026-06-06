package com.crud.project.shoppiq.config;

import com.crud.project.shoppiq.auth.jwt.JwtAuthenticationFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.crud.project.shoppiq.auth.oauth2.OAuth2SuccessHandler;

/**
 * Core Spring Security configuration for the application.
 *
 * <h3>Authentication strategy</h3>
 * <p>The application is fully stateless. Authentication is carried by a signed
 * JWT delivered to the client as an {@code HttpOnly; Secure; SameSite=Strict}
 * cookie. On every request {@link JwtAuthenticationFilter} extracts the token
 * from that cookie, validates it, and populates the {@link
 * org.springframework.security.core.context.SecurityContext}.</p>
 *
 * <h3>CSRF</h3>
 * <p>Spring's default CSRF protection is disabled here. The primary CSRF
 * mitigation is the {@code SameSite=Strict} attribute on the JWT cookie: the
 * browser will not attach the cookie to any cross-site request, making
 * classic CSRF attacks impossible on all modern browsers. If you need to
 * support older clients, re-enable CSRF and configure
 * {@code CookieCsrfTokenRepository.withHttpOnlyFalse()} so the frontend can
 * read the XSRF token from a separate readable cookie.</p>
 *
 * <h3>Session policy</h3>
 * <p>Sessions are set to {@code STATELESS} — Spring Security never creates or
 * consults an {@code HttpSession}. All state lives in the JWT.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    /**
     * Configures the main Spring Security filter chain.
     *
     * <h4>Public endpoints (no JWT required)</h4>
     * <ul>
     *   <li>{@code /login}, {@code /register}, {@code /allitems} — frontend
     *       HTML pages served directly; the browser loads them before any
     *       cookie is present.</li>
     *   <li>{@code POST /auth/login} — issues the JWT cookie; must be
     *       reachable before authentication.</li>
     *   <li>{@code POST /auth/logout} — expires the JWT cookie; must be
     *       reachable even when the cookie has already expired or is absent,
     *       so the user can always reach a clean logged-out state.</li>
     *   <li>{@code POST /user/register} — account creation; no session yet.</li>
     * </ul>
     *
     * <h4>Role-protected API endpoints</h4>
     * <ul>
     *   <li>{@code GET /item/all}, {@code GET /item/id/**} — USER or ADMIN.</li>
     *   <li>{@code POST /item/create/**}, {@code PUT /item/update/**},
     *       {@code DELETE /item/delete/**} — ADMIN only.</li>
     * </ul>
     *
     * <h4>Filter ordering</h4>
     * <p>{@link JwtAuthenticationFilter} is inserted <em>before</em>
     * {@link UsernamePasswordAuthenticationFilter} so the JWT is validated and
     * the {@code SecurityContext} is populated before any standard Spring
     * Security filter inspects the authentication state.</p>
     *
     * @param http {@link HttpSecurity} builder provided by Spring
     * @return fully configured {@link SecurityFilterChain}
     * @throws Exception if the configuration cannot be built
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                /*
                 * CSRF is disabled intentionally. The JWT cookie is issued with
                 * SameSite=Strict, which prevents the browser from attaching it
                 * to cross-origin requests — the primary CSRF vector. See class
                 * Javadoc for guidance on re-enabling CSRF for older browsers.
                 */
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // ── Public frontend routes ────────────────────────────
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/allitems",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // ── Public auth API endpoints ─────────────────────────
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()

                        // ── User registration ─────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/user/register").permitAll()

                        // ── Item read APIs — CUSTOMER or ADMIN ────────────────────
                        .requestMatchers(HttpMethod.GET, "/item/all").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/item/id/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // ── Item write APIs — ADMIN only ──────────────────────
                        .requestMatchers(HttpMethod.POST, "/item/create/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/item/update/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/item/delete/**").hasRole("ADMIN")

                        // ── Roles APIs — ADMIN only ──────────────────────
                        .requestMatchers(HttpMethod.POST, "/roles/create/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/roles/all").hasRole("ADMIN")

                        // ── Everything else requires authentication ────────────
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler)
                )

                /*
                 * No server-side session is ever created or used.
                 * All authentication state is carried in the JWT cookie.
                 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /*
                 * Validate the JWT cookie and populate SecurityContext before
                 * Spring's own username/password filter runs.
                 */
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a Spring bean.
     *
     * <p>Required so that {@code AuthService} can inject and call
     * {@code authManager.authenticate()} directly for programmatic
     * credential verification during login.</p>
     *
     * @param config {@link AuthenticationConfiguration} auto-configured by Spring Boot
     * @return the application's {@link AuthenticationManager}
     * @throws Exception if the manager cannot be retrieved
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}