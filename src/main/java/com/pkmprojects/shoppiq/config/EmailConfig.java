package com.pkmprojects.shoppiq.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Properties;
import java.util.Set;

/**
 * Configuration for email sending and Thymeleaf email template rendering.
 *
 * <p>
 * Provides a dedicated {@link TemplateEngine} for email templates that
 * is separate from the web template engine. This prevents conflicts
 * between web and email template resolution.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Configuration
public class EmailConfig {

    @Value("${shoppiq.email.smtp.host}")
    private String smtpHost;

    @Value("${shoppiq.email.smtp.port}")
    private int smtpPort;

    @Value("${shoppiq.email.smtp.username}")
    private String smtpUsername;

    @Value("${shoppiq.email.smtp.password}")
    private String smtpPassword;

    /**
     * Creates a {@link JavaMailSender} bean configured from application properties.
     *
     * @return configured mail sender
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpHost);
        mailSender.setPort(smtpPort);
        mailSender.setUsername(smtpUsername);
        mailSender.setPassword(smtpPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }

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
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        resolver.setResolvablePatterns(Set.of("*"));
        return resolver;
    }
}
