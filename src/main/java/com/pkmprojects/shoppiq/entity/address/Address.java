package com.pkmprojects.shoppiq.entity.address;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a shipping or business address.
 *
 * <p>Addresses are primarily owned by a user for customer shipping purposes,
 * but may also be referenced by a seller's business address or a store's
 * pickup address. The {@code user_id} FK is nullable to support
 * owner-agnostic usage. Each address carries a label (e.g. "Home",
 * "Office") for easy identification during checkout selection.</p>
 *
 * <p>A user may have multiple addresses, but only one can be flagged as the
 * default shipping address via the {@code isDefault} boolean. The default
 * flag is used to pre-select the shipping address during checkout. Address
 * data includes full recipient details (name, phone) and structured
 * geographic components (line1, line2, city, state, postal code, country)
 * to support both domestic and international shipping.</p>
 *
 * @author prabhatkrmishra
 * @see User
 * @see com.pkmprojects.shoppiq.entity.seller.Seller
 * @see com.pkmprojects.shoppiq.entity.seller.Store
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
     * <p>Nullable to allow addresses to be referenced by sellers and
     * stores without requiring a user owner. When present, the address
     * appears in the user's address book during checkout. Lazily loaded
     * to avoid unnecessary joins when displaying address details
     * independently.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_addresses_user")
    )
    private User user;

    /**
     * Short label for the address used for quick identification during
     * checkout selection (e.g. "Home", "Office", "Parent's House").
     *
     * <p>Required field with a maximum length of 30 characters. Displayed
     * in the address book dropdown and on shipping label previews to
     * help the customer distinguish between multiple saved addresses.</p>
     */
    @Column(nullable = false, length = 30)
    private String label;

    /**
     * Full name of the recipient at this address, shown on shipping
     * labels and delivery confirmations.
     *
     * <p>Required field with a maximum length of 100 characters. This
     * name is used by shipping carriers for delivery coordination and
     * appears on the package label. May differ from the account holder's
     * name when shipping to a different person.</p>
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * Contact phone number for delivery coordination and carrier
     * communication.
     *
     * <p>Required field with a maximum length of 15 characters to
     * accommodate international phone number formats (e.g.
     * +91-9876543210). This number is used by shipping carriers to
     * coordinate delivery timing and address verification.</p>
     */
    @Column(nullable = false, length = 15)
    private String phone;

    /**
     * Primary address line containing the street address, apartment
     * number, building name, or other delivery location details.
     *
     * <p>Required field with a maximum length of 255 characters. This
     * is the main delivery address line used by shipping carriers for
     * route planning and package delivery.</p>
     */
    @Column(nullable = false, length = 255)
    private String line1;

    /**
     * Optional secondary address line for additional delivery details
     * such as floor number, suite, landmark, or special instructions.
     *
     * <p>Maximum length of 255 characters. May be {@code null} when no
     * secondary address information is needed. This field provides
     * supplementary routing information for the shipping carrier.</p>
     */
    @Column(length = 255)
    private String line2;

    /**
     * City component of the address, used for shipping zone calculation
     * and delivery routing.
     *
     * <p>Required field with a maximum length of 100 characters. This
     * value is used by shipping carriers for zone-based rate calculation
     * and delivery time estimation.</p>
     */
    @Column(nullable = false, length = 100)
    private String city;

    /**
     * State or province component of the address, used for tax
     * calculation and regional shipping rules.
     *
     * <p>Required field with a maximum length of 100 characters. This
     * value influences applicable tax rates, shipping surcharges, and
     * delivery time estimates based on regional logistics networks.</p>
     */
    @Column(nullable = false, length = 100)
    private String state;

    /**
     * Postal or PIN code for precise geographic routing and delivery
     * zone determination.
     *
     * <p>Required field with a maximum length of 10 characters to
     * accommodate various international postal code formats (e.g.
     * 6-digit Indian PIN codes, 5-digit US ZIP codes). Used by
     * shipping carriers for automated sorting and delivery
     * optimization.</p>
     */
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    /**
     * Country component of the address, used for international shipping
     * rate calculation and customs documentation.
     *
     * <p>Required field with a maximum length of 100 characters. This
     * value determines applicable shipping methods, customs duties,
     * import regulations, and delivery timeframes for international
     * orders.</p>
     */
    @Column(nullable = false, length = 100)
    private String country;

    /**
     * Whether this is the user's default shipping address, pre-selected
     * during checkout for quick order placement.
     *
     * <p>Only one address per user may have this flag set to {@code true}.
     * The service layer enforces this invariant by clearing the default
     * flag on all other addresses when a new default is set. Defaults
     * to {@code false} for newly created addresses.</p>
     */
    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
