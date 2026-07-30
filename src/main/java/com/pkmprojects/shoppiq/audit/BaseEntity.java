package com.pkmprojects.shoppiq.audit;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Root persistence class for all JPA entities in the Shoppiq application.
 *
 * <p>Centralizes the common persistence attributes required by every
 * database entity: a database-generated primary key and optimistic
 * locking support via {@link Version}. Every entity in the domain model
 * inherits from this class to guarantee a consistent identity strategy
 * and conflict-prevention mechanism across the entire persistence layer.</p>
 *
 * <p>Optimistic locking via the {@code version} field ensures that
 * concurrent updates to the same row are detected and rejected, preventing
 * silent data loss. Hibernate automatically increments the version on
 * every flush, and a {@code OptimisticLockException} is thrown if a
 * stale transaction attempts to commit against an outdated version.</p>
 *
 * @author prabhatkrmishra
 * @see AuditableEntity
 * @since 1.0.0
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * Unique identifier of the entity.
     *
     * <p>Generated automatically by the database using the IDENTITY
     * strategy (auto-increment). This value uniquely identifies every
     * persisted record and is never reused once assigned. The identifier
     * is immutable after initial persist and serves as the primary key
     * for all JPA mapping and relationship resolution.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Entity version used for optimistic locking.
     *
     * <p>Hibernate automatically increments this value after every
     * successful update. If multiple transactions attempt to update the
     * same entity concurrently, optimistic locking prevents accidental
     * overwrites by comparing the expected version with the stored
     * version at commit time.</p>
     */
    @Version
    private Long version;
}
