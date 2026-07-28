package com.pkmprojects.shoppiq.repository.promo;

import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link PromoCode} persistence operations.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived queries</strong> — {@code findByCode} and {@code existsByCode} show
 *       simple string-based lookups.</li>
 *   <li><strong>Custom JPQL count query</strong> — {@code countByPromoCodeIdAndUserId} uses
 *       JPQL with {@code COUNT(u)} to count per-user usage records via the
 *       {@link com.pkmprojects.shoppiq.entity.promo.PromoCodeUsage} association.</li>
 *   <li><strong>{@code @Modifying} with conditional JPQL</strong> — {@code incrementUsedCountAtomically}
 *       atomically increments {@code usedCount} with an inline guard:
 *       {@code (usageLimit IS NULL OR usedCount < usageLimit)}. This is a race-condition-safe
 *       pattern for enforcing usage limits at the database level.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findByCode(String)
 *       → SELECT * FROM promo_codes WHERE code = ?
 *   existsByCode(String)
 *       → SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM promo_codes WHERE code = ?
 *   incrementUsedCountAtomically(@Modifying @Query)
 *       → UPDATE promo_codes SET used_count = used_count + 1
 *         WHERE id = ? AND (usage_limit IS NULL OR used_count &lt; usage_limit)
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    /**
     * Finds a promo code by its code string (case-insensitive lookup via uppercase stored value).
     *
     * @param code the promo code string
     * @return the promo code if found
     */
    Optional<PromoCode> findByCode(String code);

    /**
     * Checks whether a promo code with the given code string exists.
     *
     * @param code the promo code string
     * @return true if a promo code with this code exists
     */
    boolean existsByCode(String code);

    /**
     * Counts how many times a specific user has used a given promo code.
     *
     * @param promoCodeId the promo code ID
     * @param userId      the user ID
     * @return number of times the user has used this code
     */
    @Query("SELECT COUNT(u) FROM PromoCodeUsage u WHERE u.promoCode.id = :promoCodeId AND u.user.id = :userId")
    long countByPromoCodeIdAndUserId(@Param("promoCodeId") Long promoCodeId, @Param("userId") Long userId);

    /**
     * Atomically increments the usage count for a promo code.
     *
     * @param id the promo code ID
     * @return the number of rows affected (0 if the promo code was not found or the usage limit was exceeded)
     */
    @Modifying
    @Query("UPDATE PromoCode p SET p.usedCount = p.usedCount + 1 WHERE p.id = :id AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)")
    int incrementUsedCountAtomically(@Param("id") Long id);
}
