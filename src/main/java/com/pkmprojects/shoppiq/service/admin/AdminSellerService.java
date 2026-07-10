package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminSellerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.enums.VerificationStatus;

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
     * Retrieves all sellers with pagination.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated seller responses
     */
    PageResponse<AdminSellerResponse> getAllSellers(int page, int size);

    /**
     * Retrieves all sellers with the given verification status, paginated.
     *
     * @param status the verification status to filter by
     * @param page   page number (0-based)
     * @param size   page size
     * @return paginated matching sellers
     */
    PageResponse<AdminSellerResponse> getSellersByStatus(VerificationStatus status, int page, int size);

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

    /**
     * Suspends an active seller.
     *
     * <p>
     * Sets seller status to SUSPENDED and cascades to store status SUSPENDED.
     * </p>
     *
     * @param sellerId the seller ID to suspend
     * @return the updated seller
     */
    AdminSellerResponse suspendSeller(Long sellerId);

    /**
     * Unsuspends a suspended seller.
     *
     * <p>
     * Sets seller status to ACTIVE and cascades to store status DRAFT.
     * </p>
     *
     * @param sellerId the seller ID to unsuspend
     * @return the updated seller
     */
    AdminSellerResponse unsuspendSeller(Long sellerId);
}
