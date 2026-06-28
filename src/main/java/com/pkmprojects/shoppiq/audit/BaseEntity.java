package com.pkmprojects.shoppiq.audit;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Represents the root persistence class for all JPA entities in the Shoppiq application.
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
 * @author PrabhatKrMishra
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