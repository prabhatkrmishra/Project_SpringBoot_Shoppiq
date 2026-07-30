package com.pkmprojects.shoppiq.audit;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Extends {@link BaseEntity} by adding automatic auditing support.
 *
 * <p>All entities that inherit from this class automatically receive
 * creation and modification timestamps managed by Spring Data JPA.
 * Timestamps are stored as {@link Instant} in UTC, providing a
 * consistent, timezone-independent audit trail for every persisted
 * record in the system.</p>
 *
 * <p>The auditing mechanism uses the {@code AuditingEntityListener}
 * JPA entity listener, which intercepts persist and update lifecycle
 * events to populate the {@code createdAt} and {@code updatedAt}
 * fields respectively. These fields are set automatically and must
 * not be assigned manually by application code. The {@link JpaAuditConfig}
 * class enables this auditing infrastructure.</p>
 *
 * @author prabhatkrmishra
 * @see BaseEntity
 * @see com.pkmprojects.shoppiq.config.JpaAuditConfig
 * @since 1.0.0
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {

    /**
     * Timestamp indicating when the entity was first persisted.
     *
     * <p>Automatically assigned by Spring Data JPA on the initial
     * persist operation. Stored in UTC using {@link Instant} to ensure
     * timezone-independent audit records. This value is immutable after
     * creation and is never modified by subsequent update operations.</p>
     */
    @CreatedDate
    private Instant createdAt;

    /**
     * Timestamp indicating when the entity was last modified.
     *
     * <p>Updated automatically whenever Hibernate performs an update
     * operation on the entity. Stored in UTC using {@link Instant}.
     * This field reflects the most recent modification time and is
     * useful for change tracking, cache invalidation, and audit
     * reporting across the application.</p>
     */
    @LastModifiedDate
    private Instant updatedAt;
}
