package com.pkmprojects.shoppiq.audit;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Extends {@link BaseEntity} by adding automatic auditing support.
 *
 * <p>
 * All entities that inherit create this class automatically receive creation
 * and modification timestamps managed by Spring Data JPA.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores entity creation timestamp.</li>
 *     <li>Stores entity last modification timestamp.</li>
 *     <li>Delegates timestamp management to Spring Data JPA.</li>
 * </ul>
 *
 * <h2>Design Decisions</h2>
 * <ul>
 *     <li>Uses {@link Instant} to store timestamps in UTC.</li>
 *     <li>Separates auditing concerns from identity management.</li>
 * </ul>
 *
 * <h2>Used By</h2>
 * <ul>
 *     <li>User</li>
 *     <li>Item</li>
 *     <li>Order</li>
 *     <li>Cart</li>
 *     <li>Address</li>
 *     <li>Category</li>
 *     <li>Review</li>
 * </ul>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>{@code @EntityListeners(AuditingEntityListener.class)}</strong>
 *         — Registers a JPA callback listener that automatically populates
 *         {@code @CreatedDate} and {@code @LastModifiedDate} fields on persist
 *         and update operations.</li>
 *     <li><strong>{@code @CreatedDate} / {@code @LastModifiedDate}</strong>
 *         — Spring Data JPA annotations that work with {@code AuditingEntityListener}
 *         to set timestamps transparently. Requires {@code @EnableJpaAuditing}
 *         in a configuration class.</li>
 *     <li><strong>{@link Instant}</strong> — Java time-zone-agnostic timestamp
 *         stored in UTC, avoiding timezone conversion pitfalls.</li>
 *     <li><strong>Inheritance hierarchy:</strong> {@code BaseEntity → AuditableEntity
 *         → ConcreteEntity} — demonstrates layering shared persistence concerns
 *         (ID/version vs. auditing) into separate abstract classes.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {

    /**
     * Timestamp indicating when the entity was first persisted.
     *
     * <p>
     * Automatically assigned by Spring Data JPA.
     * Stored in UTC using {@link Instant}.
     * </p>
     */
    @CreatedDate
    private Instant createdAt;

    /**
     * Timestamp indicating when the entity was last modified.
     *
     * <p>
     * Updated automatically whenever Hibernate performs an update operation.
     * Stored in UTC using {@link Instant}.
     * </p>
     */
    @LastModifiedDate
    private Instant updatedAt;
}
