package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Represents a product category within the Shoppiq catalog.
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
 * @author PrabhatKrMishra
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