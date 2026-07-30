package com.pkmprojects.shoppiq.repository.seller;

import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Persistence operations for the {@link Seller} aggregate.
 *
 * <p>Provides methods to query sellers by user ID, verification status, and business email for
 * seller management and admin workflows. The repository supports paginated queries for seller
 * listing and counting by verification status for admin dashboard statistics.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface SellerRepository extends JpaRepository<Seller, Long> {

    /**
     * Finds a seller by the associated user's ID.
     *
     * @param userId the user ID
     * @return matching seller if present
     */
    Optional<Seller> findByUserId(Long userId);

    /**
     * Checks whether a seller profile already exists for the given user.
     *
     * @param userId the user ID
     * @return true if a seller exists
     */
    boolean existsByUserId(Long userId);

    /**
     * Checks whether a business email is already in use by another seller.
     *
     * @param businessEmail the business email
     * @return true if the email is taken
     */
    boolean existsByBusinessEmail(String businessEmail);

    /**
     * Finds all sellers with the given verification status, with pagination support.
     *
     * @param status   the verification status
     * @param pageable pagination information
     * @return page of matching sellers
     */
    Page<Seller> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    /**
     * Counts sellers by verification status.
     *
     * @param status the verification status
     * @return count of matching sellers
     */
    long countByVerificationStatus(VerificationStatus status);
}
