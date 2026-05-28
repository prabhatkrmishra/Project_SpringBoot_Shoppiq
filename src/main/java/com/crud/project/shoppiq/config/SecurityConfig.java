package com.crud.project.shoppiq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/register").permitAll()

                        // GET APIs -> USER or ADMIN
                        .requestMatchers("/item/all").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/item/id/**").hasAnyRole("USER", "ADMIN")

                        // WRITE APIs -> ADMIN only
                        .requestMatchers("/item/create/**").hasRole("ADMIN")
                        .requestMatchers("/item/update/**").hasRole("ADMIN")
                        .requestMatchers("/item/delete/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST,"/user/register").permitAll()

                        .anyRequest().authenticated()
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

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}