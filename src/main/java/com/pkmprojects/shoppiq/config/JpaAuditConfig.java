package com.pkmprojects.shoppiq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing across the Shoppiq application.
 *
 * <p>
 * Once enabled, Spring automatically manages fields annotated with:
 * </p>
 *
 * <ul>
 *     <li>{@code @CreatedDate}</li>
 *     <li>{@code @LastModifiedDate}</li>
 * </ul>
 *
 * <p>
 * This configuration should exist only once in the application context.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Activates auditing support.</li>
 *     <li>Registers auditing infrastructure with Spring.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}