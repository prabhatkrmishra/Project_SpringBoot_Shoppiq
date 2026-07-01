package com.pkmprojects.shoppiq.dto.seller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Address data for seller business address.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AddressRequest(

        @NotBlank
        @Size(max = 30)
        String label,

        @NotBlank
        @Size(max = 100)
        String fullName,

        @NotBlank
        @Size(max = 15)
        String phone,

        @NotBlank
        @Size(max = 255)
        String line1,

        @Size(max = 255)
        String line2,

        @NotBlank
        @Size(max = 100)
        String city,

        @NotBlank
        @Size(max = 100)
        String state,

        @NotBlank
        @Size(max = 10)
        String postalCode,

        @NotBlank
        @Size(max = 100)
        String country
) {
}
