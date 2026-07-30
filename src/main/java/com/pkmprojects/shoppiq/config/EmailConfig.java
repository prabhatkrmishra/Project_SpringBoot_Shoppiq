package com.pkmprojects.shoppiq.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Set;

/**
 * Configures a dedicated Thymeleaf {@link TemplateEngine} for email templates.
 *
 * <p>This class creates a separate template engine that resolves email
 * templates from the {@code classpath:templates/emails/} directory. By
 * isolating email template resolution from the web template engine, this
 * configuration prevents naming conflicts and allows email templates to
 * evolve independently of web-facing templates. The engine is qualified
 * with {@code @Qualifier("emailTemplateEngine")} to distinguish it from
 * the default web template engine.</p>
 *
 * <p>The email template engine is used by the verification module for
 * generating email confirmation links, password reset emails, and other
 * transactional communications. Templates are cached set to
 * {@code false} during development to allow hot-reloading, but this can
 * be overridden to {@code true} in production for better performance.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.email.impl.EmailServiceImpl
 * @since 1.0.0
 */
@Configuration
public class EmailConfig {

    /**
     * Creates a dedicated Thymeleaf {@link TemplateEngine} for email templates.
     *
     * <p>The engine is configured with a {@link ClassLoaderTemplateResolver}
     * that loads templates from {@code classpath:templates/emails/} with
     * the {@code .html} suffix and HTML template mode. The resolver uses
     * UTF-8 encoding and has caching disabled for development convenience.
     * The {@code resolvablePatterns} are set to {@code("*")} to match all
     * template names passed to the engine.</p>
     *
     * @return a fully configured {@link TemplateEngine} for email rendering
     */
    @Bean
    @Qualifier("emailTemplateEngine")
    public TemplateEngine emailTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(emailHtmlTemplateResolver());
        return engine;
    }

    private ITemplateResolver emailHtmlTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/emails/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        resolver.setResolvablePatterns(Set.of("*"));
        return resolver;
    }
}
