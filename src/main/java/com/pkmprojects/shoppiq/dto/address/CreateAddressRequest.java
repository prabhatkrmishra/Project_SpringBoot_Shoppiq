package com.pkmprojects.shoppiq.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new address in the user's address book.
 *
 * <p>This DTO is submitted to {@code POST /api/addresses} when an
 * authenticated customer wants to save a new shipping or billing address.
 * All required fields are validated using Jakarta Bean Validation
 * annotations before the request reaches the service layer, ensuring
 * that incomplete or malformed addresses are rejected at the controller
 * boundary with a structured RFC 9457 error response.</p>
 *
 * <p>The {@code isDefault} boolean is mapped from the JSON key
 * {@code "default"} using {@link com.fasterxml.jackson.annotation.JsonProperty @JsonProperty("default")}
 * to avoid a reserved-word conflict with Java's keyword set. If
 * {@code isDefault} is {@code true}, any previously-default address for
 * the user is automatically unset by the service layer. The address is
 * persisted as a child of the authenticated user's {@code User} entity
 * and is never shared across accounts.</p>
 *
 * @param label      human-readable label for the address (e.g. "Home", "Office"),
 *                   required, max 30 characters
 * @param fullName   full name of the recipient at this address,
 *                   required, max 100 characters
 * @param phone      contact phone number for delivery coordination,
 *                   required, 7-15 digits with optional leading plus sign
 * @param line1      primary street address line, required, max 255 characters
 * @param line2      secondary address line for apartment or suite details,
 *                   optional, max 255 characters
 * @param city       city or municipality name, required, max 100 characters
 * @param state      state or province name, required, max 100 characters
 * @param postalCode postal or ZIP code, required, max 10 characters
 * @param country    country name, required, max 100 characters
 * @param isDefault  whether this address should become the user's
 *                   default shipping address; serialized as
 *                   {@code "default"} in JSON; optional, defaults to false
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CreateAddressRequest(

        /**
         * Address label. Must not be blank. Max 30 characters.
         */
        @NotBlank(message = "Label is required.")
        @Size(max = 30, message = "Label cannot exceed 30 characters.")
        String label,

        /**
         * Recipient's full name. Must not be blank. Max 100 characters.
         */
        @NotBlank(message = "Full name is required.")
        @Size(max = 100, message = "Full name cannot exceed 100 characters.")
        String fullName,

        /**
         * Contact phone number. Must not be blank. 7-15 digits.
         */
        @NotBlank(message = "Phone number is required.")
        @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone number must be 7–15 digits.")
        String phone,

        /**
         * Primary address line. Must not be blank. Max 255 characters.
         */
        @NotBlank(message = "Address line 1 is required.")
        @Size(max = 255, message = "Address line 1 cannot exceed 255 characters.")
        String line1,

        /**
         * Secondary address line. Optional. Max 255 characters.
         */
        @Size(max = 255, message = "Address line 2 cannot exceed 255 characters.")
        String line2,

        /**
         * City name. Must not be blank. Max 100 characters.
         */
        @NotBlank(message = "City is required.")
        @Size(max = 100, message = "City cannot exceed 100 characters.")
        String city,

        /**
         * State or province. Must not be blank. Max 100 characters.
         */
        @NotBlank(message = "State is required.")
        @Size(max = 100, message = "State cannot exceed 100 characters.")
        String state,

        /**
         * Postal or ZIP code. Must not be blank. Max 10 characters.
         */
        @NotBlank(message = "Postal code is required.")
        @Size(max = 10, message = "Postal code cannot exceed 10 characters.")
        String postalCode,

        /**
         * Country name. Must not be blank. Max 100 characters.
         */
        @NotBlank(message = "Country is required.")
        @Size(max = 100, message = "Country cannot exceed 100 characters.")
        String country,

        /**
         * Whether this should be the user's default address.
         */
        @JsonProperty("default")
        boolean isDefault
) {
}
