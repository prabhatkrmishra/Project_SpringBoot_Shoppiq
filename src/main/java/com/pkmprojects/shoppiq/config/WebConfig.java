package com.pkmprojects.shoppiq.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * Registers the {@link ForwardedHeaderFilter} so that reverse-proxy
 * headers ({@code X-Forwarded-For}, {@code X-Forwarded-Proto}, etc.)
 * are processed and {@code request.getRemoteAddr()} returns the real
 * client IP — essential for per-IP rate limiting behind nginx / ALB / Cloudflare.
 */
@Configuration
class WebConfig {

    @Bean
    FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ForwardedHeaderFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
