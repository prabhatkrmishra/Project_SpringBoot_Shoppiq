package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Admin-facing seller response DTO.
 *
 * <p>Provides a comprehensive view of a seller account for the admin panel,
 * including business details, verification status, commission rate, and
 * associated user information.</p>
 *
 * <p><b>Pattern:</b> This response DTO flattens the {@code Seller → User}
 * relationship into a single record, exposing the user's name and email
 * alongside seller-specific fields. The static {@link #fromEntity(com.pkmprojects.shoppiq.entity.seller.Seller) fromEntity()}
 * method handles null-safe traversal of the entity graph.</p>
 *
 * @author prabhatkrmishra
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

        Instant joinedAt

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
