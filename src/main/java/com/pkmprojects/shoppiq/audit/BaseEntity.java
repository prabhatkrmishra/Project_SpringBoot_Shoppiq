package com.pkmprojects.shoppiq.audit;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;

/**
 * <strong>Spring Boot Concept:</strong> Represents the root persistence class for all JPA entities in the Shoppiq application.
 *
 * <p>
 * This class centralizes the common persistence attributes required by every
 * database entity.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Provides a database-generated primary key.</li>
 *     <li>Provides optimistic locking support.</li>
 * </ul>
 *
 * <h2>Design Decisions</h2>
 * <ul>
 *     <li>Uses {@code Long} as the identifier type for simplicity and
 *     compatibility with MySQL auto-increment columns.</li>
 *     <li>Uses {@link Version} to prevent lost updates during concurrent
 *     transactions.</li>
 *     <li>Marked as {@link MappedSuperclass} because it is not a standalone
 *     entity.</li>
 * </ul>
 *
 * <h2>Extended By</h2>
 * <ul>
 *     <li>{@link AuditableEntity}</li>
 *     <li>Any future entity requiring persistence.</li>
 * </ul>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>{@code @MappedSuperclass}</strong> — JPA inheritance strategy
 *         where the superclass is not an entity itself but its fields are inherited
 *         by subclass entities and mapped to their tables.</li>
 *     <li><strong>{@code @Id} + {@code @GeneratedValue(strategy = IDENTITY)}</strong>
 *         — Standard JPA primary key pattern using database auto-increment.</li>
 *     <li><strong>{@code @Version}</strong> — Built-in optimistic locking that
 *         prevents lost updates in concurrent environments without explicit locks.</li>
 *     <li><strong>{@code abstract}</strong> — Prevents direct instantiation;
 *         only concrete subclasses can be persisted.</li>
 *     <li><strong>Lombok {@code @Getter}</strong> — Reduces boilerplate by generating
 *         getters for all fields at compile time.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * Unique identifier of the entity.
     *
     * <p>
     * Generated automatically by the database using the IDENTITY strategy.
     * This value uniquely identifies every persisted record.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Entity version used for optimistic locking.
     *
     * <p>
     * Hibernate automatically increments this value after every successful
     * update. If multiple transactions attempt to update the same entity,
     * optimistic locking prevents accidental overwrites.
     * </p>
     */
    @Version
    private Long version;
}
