package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a shipping address.
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
 * @author PrabhatKrMishra
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
