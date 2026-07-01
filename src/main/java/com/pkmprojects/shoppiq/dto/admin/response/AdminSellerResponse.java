package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.Seller;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Admin-facing seller response DTO.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AdminSellerResponse(

        Long id,

        Long userId,

        String userName,

        String userEmail,

        String businessName,

        String businessEmail,

        String phone,

        String gstNumber,

        String panNumber,

        VerificationStatus verificationStatus,

        SellerStatus sellerStatus,

        BigDecimal commissionRate,

        BigDecimal rating,

        LocalDateTime joinedAt

) {

    public static AdminSellerResponse fromEntity(Seller seller) {
        return new AdminSellerResponse(
                seller.getId(),
                seller.getUser() != null ? seller.getUser().getId() : null,
                seller.getUser() != null ? seller.getUser().getName() : null,
                seller.getUser() != null ? seller.getUser().getEmail() : null,
                seller.getBusinessName(),
                seller.getBusinessEmail(),
                seller.getPhone(),
                seller.getGstNumber(),
                seller.getPanNumber(),
                seller.getVerificationStatus(),
                seller.getSellerStatus(),
                seller.getCommissionRate(),
                seller.getRating(),
                seller.getJoinedAt()
        );
    }
}
