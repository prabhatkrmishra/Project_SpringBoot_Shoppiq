package com.pkmprojects.shoppiq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * <strong>Spring Boot Concept:</strong> Enables Spring Data JPA auditing across the Shoppiq application.
 *
 * <p>
 * Once enabled, the {@link org.springframework.data.jpa.domain.support.AuditingEntityListener}
 * automatically populates fields annotated with {@code @CreatedDate} and
 * {@code @LastModifiedDate} on persist and update operations. This powers the
 * {@code createdAt}/{@code updatedAt} timestamps on every entity that extends
 * {@link com.pkmprojects.shoppiq.audit.AuditableEntity}.
 * </p>
 *
 * <h2>Why a separate class?</h2>
 *
 * <p>{@code @EnableJpaAuditing} imports {@code JpaAuditingRegistrar}, which
 * registers an {@code IsNewAwareAuditingHandler} bean. That bean depends on
 * a JPA {@code MappingContext}, which in turn requires a fully-configured
 * {@code EntityManagerFactory}. When {@code @EnableJpaAuditing} is placed on
 * {@code @SpringBootApplication}, the {@code @WebMvcTest} slice tries to
 * create this infrastructure during context loading — but the web slice
 * deliberately excludes JPA auto-configuration, so the required beans are
 * absent and <strong>every {@code @WebMvcTest} test fails with an
 * {@code ApplicationContext} load error</strong>.</p>
 *
 * <p>Placing the annotation on a dedicated {@code @Configuration} class
 * sidesteps this problem: Spring Boot's test slice can exclude or defer the
 * bean without tearing down the entire web-layer context.</p>
 *
 * <h2>What happens without it?</h2>
 *
 * <p>Without this configuration the auditing listener is registered on
 * entities (via {@code @EntityListeners} on {@code AuditableEntity}) but has
 * no handler to delegate to. {@code @CreatedDate} and {@code @LastModifiedDate}
 * fields remain {@code null}. Hibernate then sends an explicit {@code NULL}
 * in the INSERT statement, which overrides any MySQL {@code DEFAULT} and
 * violates the {@code NOT NULL} constraint, producing:</p>
 *
 * <pre>
 * DataIntegrityViolationException:
 *   Column 'created_at' cannot be null
 * </pre>
 *
 * <h2>Scope</h2>
 *
 * <p>This configuration must exist <strong>exactly once</strong> in the
 * application context. Do not annotate {@code @EnableJpaAuditing} on
 * {@code @SpringBootApplication} or any other configuration class.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 * @see com.pkmprojects.shoppiq.audit.AuditableEntity
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
