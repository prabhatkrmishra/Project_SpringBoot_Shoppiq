package com.pkmprojects.shoppiq.repository.seller;

import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link Seller} entities.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived queries across associations</strong> — {@code findByUserId} navigates
 *       the {@code user} relationship, generating {@code SELECT * FROM sellers WHERE user_id = ?}.</li>
 *   <li><strong>Derived exists queries</strong> — {@code existsByUserId} and
 *       {@code existsByBusinessEmail} check uniqueness without fetching the full entity.</li>
 *   <li><strong>Enum filtering with pagination</strong> — {@code findByVerificationStatus} is
 *       overloaded to accept {@link org.springframework.data.domain.Pageable}, demonstrating
 *       both basic and paginated enum-based lookups.</li>
 *   <li><strong>Derived count query</strong> — {@code countByVerificationStatus} generates
 *       {@code SELECT COUNT(*) FROM sellers WHERE verification_status = ?}.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findByUserId(Long)
 *       → SELECT * FROM sellers WHERE user_id = ?
 *   existsByUserId(Long)
 *       → SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM sellers WHERE user_id = ?
 *   existsByBusinessEmail(String)
 *       → SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM sellers WHERE business_email = ?
 *   findByVerificationStatus(VerificationStatus)
 *       → SELECT * FROM sellers WHERE verification_status = ?
 *   countByVerificationStatus(VerificationStatus)
 *       → SELECT COUNT(*) FROM sellers WHERE verification_status = ?
 * </pre>
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
     * @param status the verification status
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
