package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.seller.request.SellerProfileUpdateRequest;
import com.pkmprojects.shoppiq.dto.seller.request.SellerRegistrationRequest;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.entity.User;

/**
 * Business contract for seller profile management.
 *
 * <p>
 * Handles seller registration, profile retrieval, profile updates, and
 * deactivation. Registration creates a seller with PENDING verification
 * status. Activation occurs only after admin approval (Phase 15.3).
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Register a new seller application.</li>
 *     <li>Retrieve the authenticated seller's profile.</li>
 *     <li>Update the authenticated seller's profile.</li>
 *     <li>Deactivate (soft-delete) the seller account.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface SellerService {

    /**
     * Registers a new seller application.
     *
     * @param request the registration details
     * @param user    the authenticated user
     * @return the created seller profile
     */
    SellerResponse register(SellerRegistrationRequest request, User user);

    /**
     * Retrieves the seller profile for the authenticated user.
     *
     * @param user the authenticated user
     * @return the seller profile
     */
    SellerResponse getProfile(User user);

    /**
     * Updates the seller profile for the authenticated user.
     *
     * @param request the profile update details
     * @param user    the authenticated user
     * @return the updated seller profile
     */
    SellerResponse updateProfile(SellerProfileUpdateRequest request, User user);

    /**
     * Deactivates (soft-deletes) the seller account.
     *
     * <p>
     * Sets the seller status to {@code INACTIVE}. Open-order checks will
     * be enforced once the order-seller relationship is established.
     * </p>
     *
     * @param user the authenticated user
     */
    void deleteProfile(User user);

    /**
     * Publishes the seller's store, making it visible to customers.
     *
     * <p>
     * Sets the store status to {@code PUBLISHED}. The store must exist
     * (created automatically upon seller approval) and must be in {@code DRAFT} status.
     * </p>
     *
     * @param user the authenticated user
     */
    void publishStore(User user);
}
