package com.pkmprojects.shoppiq.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pkmprojects.shoppiq.entity.address.Address;

import java.time.Instant;

/**
 * Response payload representing a single address.
 *
 * <p>This is a <b>Java record</b> — an immutable data carrier that automatically
 * generates the constructor, {@code equals()}, {@code hashCode()}, {@code toString()},
 * and accessor methods. Records are the idiomatic choice for DTOs in modern Spring
 * Boot because they eliminate boilerplate and enforce immutability.</p>
 *
 * <p><b>API contract:</b> Returned by address endpoints (GET /api/addresses). The
 * {@code default} field is serialized as {@code "default"} in JSON via
 * {@link com.fasterxml.jackson.annotation.JsonProperty @JsonProperty}.</p>
 *
 * <p><b>Mapping pattern:</b> The static {@link #from(com.pkmprojects.shoppiq.entity.address.Address) from()}
 * factory method converts a JPA entity to this DTO, keeping the persistence layer
 * hidden from API consumers — a standard Spring Boot service-layer pattern.</p>
 *
 * @author prabhatkrmishra
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
