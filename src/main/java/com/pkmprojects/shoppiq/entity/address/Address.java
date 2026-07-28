package com.pkmprojects.shoppiq.entity.address;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * <strong>Spring Boot Concept:</strong> Represents a shipping address.
 *
 * <p>
 * Addresses are owned by a user for customer shipping, but may also be
 * referenced by a seller's business address or a store's pickup address.
 * The {@code user_id} FK is nullable to support owner-agnostic usage.
 * </p>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} for id, version, and timestamps.</li>
 *     <li>Relationship is owned by {@code Address} via a {@code user_id} FK.</li>
 *     <li>Fetch type is {@code LAZY} to avoid loading the full User graph.</li>
 * </ul>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>{@code @Entity}</strong> — Marks this class as a JPA entity
 *         managed by the {@code EntityManager}.</li>
 *     <li><strong>{@code @Table(name = "addresses")}</strong> — Maps the entity
 *         to a specific database table; the table name differs from the class name.</li>
 *     <li><strong>{@code @ManyToOne(fetch = FetchType.LAZY)}</strong> — A
 *         many-to-one relationship with lazy loading to avoid N+1 queries.
 *         The owning side holds the foreign key via {@code @JoinColumn}.</li>
 *     <li><strong>{@code @ForeignKey}</strong> — Names the foreign key constraint
 *         for readable schema migrations and easier debugging.</li>
 *     <li><strong>Lombok {@code @Builder}, {@code @NoArgsConstructor},
 *         {@code @AllArgsConstructor}</strong> — The Builder pattern for
 *         constructing entities with many fields, while JPA requires a
 *         no-arg constructor.</li>
 *     <li><strong>{@code @EqualsAndHashCode(callSuper = true)}</strong>
 *         — Includes the inherited {@code id} and {@code version} fields in
 *         equality checks. Avoids issues with generated proxy objects.</li>
 *     <li><strong>Nullable FK via {@code user_id}</strong> — Demonstrates a
 *         nullable foreign key to support multiple ownership contexts
 *         (user, seller, store).</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Address extends AuditableEntity {

    /**
     * The user who owns this address, if any.
     *
     * <p>Nullable to allow addresses to be referenced by sellers
     * and stores without requiring a user owner.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_addresses_user")
    )
    private User user;

    /**
     * Short label for the address (e.g. Home, Office).
     */
    @Column(nullable = false, length = 30)
    private String label;

    /**
     * Full name of the recipient.
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * Contact phone number.
     */
    @Column(nullable = false, length = 15)
    private String phone;

    /**
     * Primary address line.
     */
    @Column(nullable = false, length = 255)
    private String line1;

    /**
     * Optional secondary address line.
     */
    @Column(length = 255)
    private String line2;

    /**
     * City.
     */
    @Column(nullable = false, length = 100)
    private String city;

    /**
     * State or province.
     */
    @Column(nullable = false, length = 100)
    private String state;

    /**
     * Postal or PIN code.
     */
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    /**
     * Country.
     */
    @Column(nullable = false, length = 100)
    private String country;

    /**
     * Whether this is the user's default shipping address.
     *
     * <p>Only one address per user may have this flag set to {@code true}.</p>
     */
    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
