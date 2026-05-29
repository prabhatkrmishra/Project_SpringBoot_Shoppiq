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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Core security configuration for the application.
 * Defines authentication rules, JWT filter placement, session policy,
 * password encoding, and remember-me services.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures the main Spring Security filter chain.
     * Disables CSRF (stateless JWT), sets authorization rules,
     * makes session STATELESS, configures form login, remember-me, logout,
     * and adds JWT filter before UsernamePasswordAuthenticationFilter.
     *
     * @param http HttpSecurity to configure
     * @return SecurityFilterChain bean
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        try {
            http
                    .csrf(csrf -> csrf.disable())

                    .authorizeHttpRequests(auth -> auth

                            .requestMatchers(
                                    "/login",
                                    "/register",
                                    "/allitems",
                                    "/auth/login"
                            ).permitAll()

                            // GET APIs -> USER or ADMIN
                            .requestMatchers("/item/all").hasAnyRole("USER", "ADMIN")
                            .requestMatchers("/item/id/**").hasAnyRole("USER", "ADMIN")

                            // WRITE APIs -> ADMIN only
                            .requestMatchers("/item/create/**").hasRole("ADMIN")
                            .requestMatchers("/item/update/**").hasRole("ADMIN")
                            .requestMatchers("/item/delete/**").hasRole("ADMIN")

                            .requestMatchers(HttpMethod.POST, "/user/register").permitAll()

                            .anyRequest().authenticated()
                    )

                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )

                    .formLogin(form -> form
                                    .loginPage("/login")
                                    .loginProcessingUrl("/login")
                                    .defaultSuccessUrl("/item/all", true)
                            //.permitAll() have in authorizeHttpRequests
                    )

                    .rememberMe(remember -> remember
                            .userDetailsService(userDetailsService)
                            .key("aVeryLongRandomSecretKeyForSecurity123")
                            .tokenValiditySeconds(7 * 24 * 60 * 60)
                            .rememberMeCookieName("remember_me")
                    )

                    .logout(logout -> logout
                            .deleteCookies("remember_me")
                    )
            ;

            http.addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );
            return http.build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure SecurityFilterChain", e);
        }
    }

    /**
     * Provides BCrypt password encoder for hashing user passwords.
     * Used by DaoAuthenticationProvider during login.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        try {
            return new BCryptPasswordEncoder();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PasswordEncoder bean", e);
        }
    }

    /**
     * Exposes AuthenticationManager as a bean.
     * Required for programmatic authentication in AuthService.
     *
     * @param config AuthenticationConfiguration provided by Spring
     * @return AuthenticationManager instance
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        try {
            return config.getAuthenticationManager();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve AuthenticationManager", e);
        }
    }
}