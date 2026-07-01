package com.pkmprojects.shoppiq.dto.seller.response;

import com.pkmprojects.shoppiq.entity.Address;
import com.pkmprojects.shoppiq.entity.Seller;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for seller profile.
 *
 * @author PrabhatKrMishra
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

        LocalDateTime joinedAt
) {

    public record AddressResponse(
            Long id,
            String label,
            String fullName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country
    ) {
        static AddressResponse fromEntity(Address address) {
            if (address == null) return null;
            return new AddressResponse(
                    address.getId(),
                    address.getLabel(),
                    address.getFullName(),
                    address.getPhone(),
                    address.getLine1(),
                    address.getLine2(),
                    address.getCity(),
                    address.getState(),
                    address.getPostalCode(),
                    address.getCountry()
            );
        }
    }

    public static SellerResponse fromEntity(Seller seller) {
        return new SellerResponse(
                seller.getId(),
                seller.getUser() != null ? seller.getUser().getId() : null,
                seller.getBusinessName(),
                seller.getBusinessEmail(),
                seller.getPhone(),
                seller.getGstNumber(),
                seller.getPanNumber(),
                AddressResponse.fromEntity(seller.getBusinessAddress()),
                seller.getVerificationStatus(),
                seller.getSellerStatus(),
                seller.getCommissionRate(),
                seller.getRating() != null ? seller.getRating().doubleValue() : null,
                seller.getJoinedAt()
        );
    }
}
