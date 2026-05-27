package com.crud.project.shoppiq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // GET APIs -> USER or ADMIN
                        .requestMatchers("/item/all").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/item/id/**").hasAnyRole("USER", "ADMIN")

                        // WRITE APIs -> ADMIN only
                        .requestMatchers("/item/create/**").hasRole("ADMIN")
                        .requestMatchers("/item/update/**").hasRole("ADMIN")
                        .requestMatchers("/item/delete/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {

        UserDetails user1 = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("policy64"))
                .roles("ADMIN")
                .build();

        UserDetails user2 = User.builder()
                .username("user")
                .password(passwordEncoder.encode("service64"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user1, user2);
    }
}