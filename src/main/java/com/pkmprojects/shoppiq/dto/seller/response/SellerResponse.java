package com.pkmprojects.shoppiq.dto.seller.response;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for the seller profile detail view.
 *
 * <p>This record flattens the {@code Seller → User → Address} entity
 * graph into a single DTO for API consumers. It reuses the shared
 * {@link com.pkmprojects.shoppiq.dto.address.AddressResponse} for the
 * business address and provides a comprehensive view of the seller's
 * profile including business details, verification status, commission
 * configuration, and performance metrics.</p>
 *
 * <p>The static {@link #fromEntity(Seller)} factory method handles
 * nullable associations gracefully, returning null for fields when
 * the associated user or address entity does not exist. The
 * {@code verificationStatus} tracks the seller's onboarding progress,
 * while {@code sellerStatus} tracks the operational account state.</p>
 *
 * @param id                 unique identifier of the seller entity
 * @param userId             identifier of the user account that owns this seller
 *                           profile; nullable if the user has been removed
 * @param businessName       registered business name of the seller
 * @param businessEmail      business email address for commercial correspondence
 * @param phone              contact phone number for the seller
 * @param gstNumber          GST identification number; nullable for sellers
 *                           operating outside GST jurisdiction
 * @param panNumber          PAN number; exactly 10 characters for Indian sellers
 * @param businessAddress    nested address response for the seller's
 *                           business address; nullable if not yet provided
 * @param verificationStatus onboarding verification state
 *                           (PENDING, VERIFIED, REJECTED)
 * @param sellerStatus       operational account status
 *                           (ACTIVE, SUSPENDED, etc.)
 * @param commissionRate     platform commission percentage applied to the
 *                           seller's sales; typically between 0 and 100
 * @param rating             average customer rating across all seller products,
 *                           ranging from 0.0 to 5.0; nullable if no reviews exist
 * @param joinedAt           timestamp when the seller registered on the platform
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record SellerResponse(

        /**
         * Unique identifier of the seller.
         */
        Long id,

        /**
         * ID of the user who owns this seller profile.
         */
        Long userId,

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
         * Business address. Nullable if not yet provided.
         */
        AddressResponse businessAddress,

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
        Double rating,

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
