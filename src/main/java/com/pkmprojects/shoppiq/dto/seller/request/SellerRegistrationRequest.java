package com.pkmprojects.shoppiq.dto.seller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for seller registration.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record SellerRegistrationRequest(

        @NotBlank(message = "Business name is required")
        @Size(max = 255)
        String businessName,

        @NotBlank(message = "Business email is required")
        @Email(message = "Invalid business email format")
        @Size(max = 255)
        String businessEmail,

        @NotBlank(message = "Phone number is required")
        @Size(max = 15)
        String phone,

        @Size(max = 20)
        String gstNumber,

        @NotBlank(message = "PAN number is required")
        @Size(min = 10, max = 10, message = "PAN number must be exactly 10 characters")
        String panNumber
) {
}
