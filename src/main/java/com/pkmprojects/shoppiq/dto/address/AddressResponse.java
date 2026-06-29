package com.pkmprojects.shoppiq.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pkmprojects.shoppiq.entity.Address;

import java.time.Instant;

/**
 * Response payload representing a single address.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AddressResponse(

        Long id,
        String label,
        String fullName,
        String phone,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country,

        @JsonProperty("default")
        boolean isDefault,

        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Constructs an {@link AddressResponse} from an {@link Address} entity.
     *
     * @param address source entity
     * @return response DTO
     */
    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getFullName(),
                address.getPhone(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.isDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}
