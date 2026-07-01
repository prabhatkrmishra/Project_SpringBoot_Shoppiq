package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminSellerResponse;
import com.pkmprojects.shoppiq.enums.VerificationStatus;

import java.util.List;

/**
 * Admin service for seller management.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>List sellers by verification status.</li>
 *     <li>Approve pending seller applications.</li>
 *     <li>Reject pending seller applications.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface AdminSellerService {

    /**
     * Retrieves all sellers.
     *
     * @return list of all sellers
     */
    List<AdminSellerResponse> getAllSellers();

    /**
     * Retrieves all sellers with the given verification status.
     *
     * @param status the verification status to filter by
     * @return list of matching sellers
     */
    List<AdminSellerResponse> getSellersByStatus(VerificationStatus status);

    /**
     * Approves a pending seller application.
     *
     * <p>
     * Sets verification status to APPROVED, seller status to ACTIVE,
     * auto-creates a store in DRAFT status, and grants the ROLE_SELLER
     * role to the user.
     * </p>
     *
     * @param sellerId the seller ID to approve
     * @return the updated seller
     */
    AdminSellerResponse approveSeller(Long sellerId);

    /**
     * Rejects a pending seller application.
     *
     * <p>
     * Sets verification status to REJECTED and seller status to INACTIVE.
     * </p>
     *
     * @param sellerId the seller ID to reject
     * @return the updated seller
     */
    AdminSellerResponse rejectSeller(Long sellerId);
}
