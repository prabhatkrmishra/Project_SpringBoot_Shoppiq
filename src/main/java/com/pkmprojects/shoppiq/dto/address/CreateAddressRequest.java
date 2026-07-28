package com.pkmprojects.shoppiq.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new address.
 *
 * <p>This <b>Java record</b> demonstrates Jakarta Bean Validation annotations to
 * enforce the API contract at the controller boundary. When validation fails,
 * Spring Boot returns a structured {@code 400 Bad Request} response automatically.</p>
 *
 * <p><b>Validation patterns shown:</b></p>
 * <ul>
 *   <li>{@link jakarta.validation.constraints.NotBlank @NotBlank} — required fields</li>
 *   <li>{@link jakarta.validation.constraints.Size @Size} — maximum length constraints</li>
 *   <li>{@link jakarta.validation.constraints.Pattern @Pattern} — phone number format validation</li>
 * </ul>
 *
 * <p><b>Serialization:</b> The {@code default} field uses
 * {@link com.fasterxml.jackson.annotation.JsonProperty @JsonProperty("default")}
 * to map the Java field name to a JavaScript reserved word in JSON.</p>
 *
 * @author prabhatkrmishra
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
