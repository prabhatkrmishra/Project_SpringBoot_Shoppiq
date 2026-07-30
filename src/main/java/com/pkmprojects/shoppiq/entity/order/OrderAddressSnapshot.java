package com.pkmprojects.shoppiq.entity.order;

import com.pkmprojects.shoppiq.entity.address.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Immutable snapshot of shipping address fields captured at checkout time.
 *
 * <p>Stored directly on the {@link Order} so that historical orders remain
 * accurate even if the customer later edits or deletes their address book
 * entry. Created from a live {@link Address} via the {@code from()} factory
 * method. This snapshot ensures that order history, invoices, and customer
 * support references always reflect the address as it was at the moment
 * of purchase.</p>
 *
 * <p>This is an {@code @Embeddable} value object, not an independent
 * entity. Its fields are mapped to columns on the {@code orders} table
 * with a {@code shipping_} prefix via {@code @AttributeOverrides} in
 * the owning {@link Order} entity. The snapshot is intentionally
 * immutable after creation to preserve audit integrity.</p>
 *
 * @author prabhatkrmishra
 * @see Order
 * @see com.pkmprojects.shoppiq.entity.address.Address
 * @since 1.0.0
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddressSnapshot {

    /**
     * Full name of the recipient at the time the order was placed.
     *
     * <p>Captured from {@link com.pkmprojects.shoppiq.entity.address.Address#getFullName()}.
     * Maximum length of 100 characters. This field preserves the
     * recipient identity as it appeared on the shipping label at
     * checkout, independent of any subsequent profile changes.</p>
     */
    @Column(name = "shipping_full_name", length = 100)
    private String fullName;

    /**
     * Contact phone number for delivery coordination at the time
     * the order was placed.
     *
     * <p>Captured from {@link com.pkmprojects.shoppiq.entity.address.Address#getPhone()}.
     * Maximum length of 15 characters to accommodate international
     * phone number formats.</p>
     */
    @Column(name = "shipping_phone", length = 15)
    private String phone;

    /**
     * Primary address line (street address, apartment number, etc.)
     * at the time the order was placed.
     *
     * <p>Captured from {@link com.pkmprojects.shoppiq.entity.address.Address#getLine1()}.
     * Maximum length of 255 characters. This is the main delivery
     * address line used by shipping carriers.</p>
     */
    @Column(name = "shipping_line1", length = 255)
    private String line1;

    /**
     * Optional secondary address line for additional delivery details
     * (e.g. building name, floor, suite number).
     *
     * <p>Captured from {@link com.pkmprojects.shoppiq.entity.address.Address#getLine2()}.
     * Maximum length of 255 characters. May be {@code null} when no
     * secondary address information is provided.</p>
     */
    @Column(name = "shipping_line2", length = 255)
    private String line2;

    /**
     * City component of the shipping address at the time the order
     * was placed.
     *
     * <p>Captured from {@link com.pkmprojects.shoppiq.entity.address.Address#getCity()}.
     * Maximum length of 100 characters.</p>
     */
    @Column(name = "shipping_city", length = 100)
    private String city;

    /**
     * State or province component of the shipping address at the time
     * the order was placed.
     *
     * <p>Captured from {@link com.pkmprojects.shoppiq.entity.address.Address#getState()}.
     * Maximum length of 100 characters.</p>
     */
    @Column(name = "shipping_state", length = 100)
    private String state;

    /**
     * Postal or PIN code component of the shipping address at the
     * time the order was placed.
     *
     * <p>Captured from {@link com.pkmprojects.shoppiq.entity.address.Address#getPostalCode()}.
     * Maximum length of 10 characters to accommodate various
     * international postal code formats.</p>
     */
    @Column(name = "shipping_postal_code", length = 10)
    private String postalCode;

    /**
     * Country component of the shipping address at the time the order
     * was placed.
     *
     * <p>Captured from {@link com.pkmprojects.shoppiq.entity.address.Address#getCountry()}.
     * Maximum length of 100 characters. Used for international shipping
     * rate calculation and customs documentation.</p>
     */
    @Column(name = "shipping_country", length = 100)
    private String country;

    /**
     * Creates a snapshot from a live {@link Address} entity.
     *
     * @param address the address to copy
     * @return snapshot with copied fields, or {@code null} if address is null
     */
    public static OrderAddressSnapshot from(Address address) {
        if (address == null) return null;
        return OrderAddressSnapshot.builder()
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }
}
