package com.pkmprojects.shoppiq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing for automatic timestamp population.
 *
 * <p>This configuration activates the {@code @EnableJpaAuditing}
 * infrastructure, which automatically populates {@code createdAt} and
 * {@code updatedAt} timestamps on any entity that extends
 * {@link com.pkmprojects.shoppiq.audit.AuditableEntity}. The auditing
 * mechanism hooks into JPA's lifecycle callbacks and fills in the
 * audit fields before each {@code INSERT} and {@code UPDATE} operation.</p>
 *
 * <p>Architecturally, this class must exist exactly once in the application
 * context. Having multiple {@code @EnableJpaAuditing} declarations causes
 * duplicate auditor bean registration and unpredictable behavior. The
 * auditing infrastructure also supports {@code @LastModifiedBy} and
 * {@code @LastModifiedDate} annotations, though the Shoppiq application
 * currently uses only timestamp auditing.</p>
 *
 * @author PrabhatKrMishra
 * @see com.pkmprojects.shoppiq.audit.AuditableEntity
 * @since 1.0.0
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
