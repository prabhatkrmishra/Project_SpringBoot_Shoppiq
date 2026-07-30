package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Admin-facing seller response DTO for seller management.
 *
 * <p>This record provides a comprehensive view of a seller account for
 * administrators, including business details, associated user identity,
 * verification status, commission configuration, and performance
 * metrics. It is returned by the admin seller list and detail endpoints
 * and is designed for the seller management UI where administrators
 * review, approve, and manage seller onboarding.</p>
 *
 * <p>The static {@link #fromEntity(Seller)} factory method handles the
 * entity-to-DTO conversion with null-safe access to the associated
 * user entity. The {@code verificationStatus} tracks the seller's
 * onboarding progress (PENDING, VERIFIED, REJECTED), while
 * {@code sellerStatus} tracks the operational state (ACTIVE,
 * SUSPENDED). The {@code commissionRate} represents the platform's
 * percentage-based fee on the seller's sales.</p>
 *
 * @param id                 unique identifier of the seller entity
 * @param userId             identifier of the user account that owns this seller
 *                           profile; nullable if the user has been removed
 * @param userName           display name of the owning user account
 * @param userEmail          email address of the owning user account
 * @param businessName       registered business name of the seller
 * @param businessEmail      business email address for commercial correspondence
 * @param phone              contact phone number for the seller
 * @param gstNumber          GST identification number; nullable for sellers
 *                           operating outside GST jurisdiction
 * @param panNumber          PAN number; exactly 10 characters for Indian sellers
 * @param verificationStatus onboarding verification state
 *                           (PENDING, VERIFIED, REJECTED)
 * @param sellerStatus       operational account status
 *                           (ACTIVE, SUSPENDED, etc.)
 * @param commissionRate     platform commission percentage applied to
 *                           the seller's sales; typically between 0 and 100
 * @param rating             average customer rating across all seller products,
 *                           ranging from 0.0 to 5.0; nullable if no reviews exist
 * @param joinedAt           timestamp when the seller registered on the platform
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminSellerResponse(

        /**
         * Unique identifier of the seller.
         */
        Long id,

        /**
         * ID of the user who owns this seller profile.
         */
        Long userId,

        /**
         * User's display name.
         */
        String userName,

        /**
         * User's email address.
         */
        String userEmail,

        /**
         * Business name of the seller.
         */
        String businessName,

        /**
         * Business email address.
         */
        String businessEmail,

        /**
         * Contact phone number.
         */
        String phone,

        /**
         * GST identification number (optional).
         */
        String gstNumber,

        /**
         * PAN number (exactly 10 characters).
         */
        String panNumber,

        /**
         * Verification status (PENDING, VERIFIED, REJECTED).
         */
        VerificationStatus verificationStatus,

        /**
         * Account status (ACTIVE, SUSPENDED, etc.).
         */
        SellerStatus sellerStatus,

        /**
         * Commission rate applied to seller's sales (percentage).
         */
        BigDecimal commissionRate,

        /**
         * Average customer rating (0.0–5.0). Nullable if no reviews yet.
         */
        BigDecimal rating,

        /**
         * Timestamp when the seller joined the platform.
         */
        Instant joinedAt

) {

    /**
     * Creates a response DTO from the given entity.
     *
     * @param seller the seller entity
     * @return populated response DTO
     */
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
