package com.pkmprojects.shoppiq.dto.seller.response;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for seller profile.
 *
 * <p>This Java record flattens the {@code Seller → User → Address} entity
 * graph into a single DTO. It reuses the shared
 * {@link com.pkmprojects.shoppiq.dto.address.AddressResponse} for the
 * business address.</p>
 *
 * <p><b>Null-safe mapping:</b> The {@link #fromEntity(com.pkmprojects.shoppiq.entity.seller.Seller) fromEntity()}
 * method handles nullable associations (user, address) gracefully, returning
 * null for fields when the associated entity does not exist.</p>
 *
 * <p><b>Verification workflow:</b> The {@code verificationStatus} and
 * {@code sellerStatus} fields track the seller's onboarding progress,
 * from PENDING through VERIFIED, and ACTIVE through SUSPENDED.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record SellerResponse(

        Long id,

        Long userId,

        String businessName,

        String businessEmail,

        String phone,

        String gstNumber,

        String panNumber,

        AddressResponse businessAddress,

        VerificationStatus verificationStatus,

        SellerStatus sellerStatus,

        BigDecimal commissionRate,

        Double rating,

        Instant joinedAt
) {

    public static SellerResponse fromEntity(Seller seller) {
        Address address = seller.getBusinessAddress();
        return new SellerResponse(
                seller.getId(),
                seller.getUser() != null ? seller.getUser().getId() : null,
                seller.getBusinessName(),
                seller.getBusinessEmail(),
                seller.getPhone(),
                seller.getGstNumber(),
                seller.getPanNumber(),
                address != null ? AddressResponse.from(address) : null,
                seller.getVerificationStatus(),
                seller.getSellerStatus(),
                seller.getCommissionRate(),
                seller.getRating() != null ? seller.getRating().doubleValue() : null,
                seller.getJoinedAt()
        );
    }
}
