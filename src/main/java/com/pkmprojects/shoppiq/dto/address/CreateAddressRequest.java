package com.pkmprojects.shoppiq.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new address.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record CreateAddressRequest(

        @NotBlank(message = "Label is required.")
        @Size(max = 30, message = "Label cannot exceed 30 characters.")
        String label,

        @NotBlank(message = "Full name is required.")
        @Size(max = 100, message = "Full name cannot exceed 100 characters.")
        String fullName,

        @NotBlank(message = "Phone number is required.")
        @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone number must be 7–15 digits.")
        String phone,

        @NotBlank(message = "Address line 1 is required.")
        @Size(max = 255, message = "Address line 1 cannot exceed 255 characters.")
        String line1,

        @Size(max = 255, message = "Address line 2 cannot exceed 255 characters.")
        String line2,

        @NotBlank(message = "City is required.")
        @Size(max = 100, message = "City cannot exceed 100 characters.")
        String city,

        @NotBlank(message = "State is required.")
        @Size(max = 100, message = "State cannot exceed 100 characters.")
        String state,

        @NotBlank(message = "Postal code is required.")
        @Size(max = 10, message = "Postal code cannot exceed 10 characters.")
        String postalCode,

        @NotBlank(message = "Country is required.")
        @Size(max = 100, message = "Country cannot exceed 100 characters.")
        String country,

        @JsonProperty("default")
        boolean isDefault
) {
}
