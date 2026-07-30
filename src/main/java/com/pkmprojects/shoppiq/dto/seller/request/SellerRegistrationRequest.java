package com.pkmprojects.shoppiq.dto.seller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for registering a new seller account on the platform.
 *
 * <p>This record carries all required business information for seller
 * onboarding. It is submitted to the seller registration endpoint and
 * validated using Jakarta Bean Validation before reaching the service
 * layer. Upon successful registration, the seller profile is created
 * with PENDING verification status, requiring admin approval before
 * the seller can list products.</p>
 *
 * <p>The {@code panNumber} is constrained to exactly 10 characters to
 * match the Indian PAN card format. The {@code gstNumber} is optional
 * for sellers operating outside GST jurisdiction. The {@code businessEmail}
 * must be a valid email format and is used for commercial correspondence
 * with the seller.</p>
 *
 * @param businessName  registered business name of the seller, required,
 *                      max 255 characters; displayed in the storefront
 * @param businessEmail business email address for commercial correspondence,
 *                      required, max 255 characters; must be valid format
 * @param phone         contact phone number for the seller, required, max 15
 *                      characters; used for delivery coordination
 * @param gstNumber     GST identification number, optional, max 20 characters;
 *                      nullable for sellers outside GST jurisdiction
 * @param panNumber     PAN number, required, exactly 10 characters; validated
 *                      against the Indian PAN card format
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record SellerRegistrationRequest(

        /**
         * Business name. Must not be blank. Max 255 characters.
         */
        @NotBlank(message = "Business name is required")
        @Size(max = 255)
        String businessName,

        /**
         * Business email address. Must be valid. Max 255 characters.
         */
        @NotBlank(message = "Business email is required")
        @Email(message = "Invalid business email format")
        @Size(max = 255)
        String businessEmail,

        /**
         * Contact phone number. Must not be blank. Max 15 characters.
         */
        @NotBlank(message = "Phone number is required")
        @Size(max = 15)
        String phone,

        /**
         * GST identification number. Optional. Max 20 characters.
         */
        @Size(max = 20)
        String gstNumber,

        /**
         * PAN number. Must not be blank. Exactly 10 characters.
         */
        @NotBlank(message = "PAN number is required")
        @Size(min = 10, max = 10, message = "PAN number must be exactly 10 characters")
        String panNumber
) {
}
