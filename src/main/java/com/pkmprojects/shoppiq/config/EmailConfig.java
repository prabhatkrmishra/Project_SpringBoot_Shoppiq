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
 * <strong>Spring Boot Concept:</strong> {@code @Configuration} class that
 * configures a dedicated Thymeleaf {@link TemplateEngine} for email
 * templates, separate from the web template engine.
 *
 * <p>The {@code JavaMailSender} is auto-configured by Spring Boot via
 * {@code spring.mail.*} properties. This configuration only provides
 * the template resolution for email templates used by the verification
 * module (email confirmation, password reset codes).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
public class EmailConfig {

    /**
     * Creates a dedicated Thymeleaf {@link TemplateEngine} for email templates.
     *
     * <p>
     * This engine resolves templates from {@code classpath:templates/emails/}
     * with HTML mode. It is qualified to avoid conflicts with the web
     * template engine.
     * </p>
     *
     * @return email template engine
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
