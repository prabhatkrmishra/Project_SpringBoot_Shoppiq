package com.pkmprojects.shoppiq.dto.seller.request;

import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a seller's profile information.
 *
 * <p>All fields in this record are optional, implementing PATCH semantics
 * where only the provided fields are updated. This is achieved by not
 * using {@code @NotNull} or {@code @NotBlank} on any field; the service
 * layer checks each field for non-null/non-blank before applying the
 * update, preserving the existing value for any omitted field.</p>
 *
 * <p>The {@code businessAddress} field accepts a nested
 * {@link CreateAddressRequest} for updating the seller's business
 * address. When provided, it replaces the entire address. When omitted,
 * the existing address remains unchanged. The {@code @Email} annotation
 * on {@code businessEmail} only validates when the value is non-null,
 * preventing validation errors on omitted fields.</p>
 *
 * @param businessName    new business name; optional, max 255 characters;
 *                        null = no change
 * @param businessEmail   new business email address; optional, must be
 *                        valid format if provided, max 255 characters;
 *                        null = no change
 * @param phone           new contact phone number; optional, max 15 characters;
 *                        null = no change
 * @param gstNumber       new GST identification number; optional, max 20
 *                        characters; null = no change
 * @param panNumber       new PAN number; optional, max 10 characters;
 *                        null = no change
 * @param businessAddress new business address; optional; when provided,
 *                        replaces the entire existing address; null = no change
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record SellerProfileUpdateRequest(

        /**
         * Business name. Optional — only provided fields are updated.
         */
        @Size(max = 255)
        String businessName,

        /**
         * Business email address. Optional — must be valid if provided.
         */
        @Email(message = "Invalid business email format")
        @Size(max = 255)
        String businessEmail,

        /**
         * Contact phone number. Optional — max 15 characters.
         */
        @Size(max = 15)
        String phone,

        /**
         * GST identification number. Optional — max 20 characters.
         */
        @Size(max = 20)
        String gstNumber,

        /**
         * PAN number. Optional — max 10 characters.
         */
        @Size(max = 10)
        String panNumber,

        /**
         * Business address. Optional — nested DTO with its own validation.
         */
        CreateAddressRequest businessAddress

) {
}
