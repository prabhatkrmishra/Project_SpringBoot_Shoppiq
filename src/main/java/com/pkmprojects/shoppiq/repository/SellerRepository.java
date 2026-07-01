package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Seller;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Seller} entities.
 *
 * @author PrabhatKrMishra
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
     * Finds all sellers with the given verification status.
     *
     * @param status the verification status
     * @return list of matching sellers
     */
    List<Seller> findByVerificationStatus(VerificationStatus status);

    /**
     * Counts sellers by verification status.
     *
     * @param status the verification status
     * @return count of matching sellers
     */
    long countByVerificationStatus(VerificationStatus status);
}
