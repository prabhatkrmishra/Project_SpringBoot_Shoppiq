package com.pkmprojects.shoppiq.entity.category;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * <strong>Spring Boot Concept:</strong> Represents a product category within the Shoppiq catalog.
 *
 * <p>
 * Categories classify products into logical groups such as
 * "Electronics", "Fashion", and "Home Appliances". Each category
 * has a human-readable name and a URL-friendly slug.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores category metadata.</li>
 *     <li>Acts as the parent entity for catalog classification.</li>
 *     <li>Provides persistence only; business rules belong in the service layer.</li>
 * </ul>
 *
 * <h2>Design Decisions</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} to inherit identity, optimistic locking,
 *     and auditing support.</li>
 *     <li>Slug generation is intentionally delegated to the service layer.</li>
 *     <li>Name and slug are enforced as unique at the database level.</li>
 * </ul>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Unique constraints via {@code @UniqueConstraint}</strong>
 *         — Two separate constraints ({@code uk_categories_name},
 *         {@code uk_categories_slug}) enforce uniqueness at the database
 *         level, preventing duplicate entries regardless of application logic.</li>
 *     <li><strong>{@code @Entity} with no explicit relationships</strong>
 *         — A simple "reference" entity that is referenced by other entities
 *         (via {@code @ManyToOne}) but has no collection mappings of its own.</li>
 *     <li><strong>Jakarta Validation ({@code @NotBlank}, {@code @Size})</strong>
 *         — Ensures data integrity before reaching the database. Validation
 *         is triggered automatically when using {@code @Valid} in controllers
 *         or {@code @Validated} in services.</li>
 *     <li><strong>Service-layer delegation</strong> — Slug generation is not
 *         done in the entity. This follows the principle that entities should
 *         be plain data holders; business logic belongs in service classes.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_categories_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_categories_slug", columnNames = "slug")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Category extends AuditableEntity {

    /**
     * Human-readable category name.
     */
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * URL-friendly identifier.
     */
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String slug;

    /**
     * Optional category description.
     */
    @Size(max = 255)
    @Column(length = 255)
    private String description;

}
