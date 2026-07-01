package com.pkmprojects.shoppiq.dto.seller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating seller profile.
 *
 * <p>All fields are optional — only provided fields will be updated.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record SellerProfileUpdateRequest(

        @Size(max = 255)
        String businessName,

        @Email(message = "Invalid business email format")
        @Size(max = 255)
        String businessEmail,

        @Size(max = 15)
        String phone,

        @Size(max = 20)
        String gstNumber,

        @Size(max = 10)
        String panNumber,

        AddressRequest businessAddress

) {
}
