package com.pkmprojects.shoppiq.dto.seller.request;

import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * <strong>Spring Boot Concept:</strong> Request DTO for updating seller profile.
 *
 * <p>All fields are optional — only provided fields will be updated. This is
 * achieved by NOT using {@code @NotNull} or {@code @NotBlank} on any field
 * (except implicit constraints like {@code @Email} which only validate when
 * the value is non-null).</p>
 *
 * <p><b>Partial update pattern:</b> The service layer checks each field for
 * non-null/non-blank before applying the update, enabling PATCH semantics
 * where the client sends only changed fields.</p>
 *
 * @author prabhatkrmishra
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

        CreateAddressRequest businessAddress

) {
}
