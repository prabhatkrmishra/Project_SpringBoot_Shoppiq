package com.pkmprojects.shoppiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Immutable snapshot of shipping address fields captured at checkout time.
 *
 * <p>Stored directly on the {@link Order} so that historical orders
 * remain accurate even if the customer later edits or deletes their
 * address book entry.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddressSnapshot {

    @Column(name = "shipping_full_name", length = 100)
    private String fullName;

    @Column(name = "shipping_phone", length = 15)
    private String phone;

    @Column(name = "shipping_line1", length = 255)
    private String line1;

    @Column(name = "shipping_line2", length = 255)
    private String line2;

    @Column(name = "shipping_city", length = 100)
    private String city;

    @Column(name = "shipping_state", length = 100)
    private String state;

    @Column(name = "shipping_postal_code", length = 10)
    private String postalCode;

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
