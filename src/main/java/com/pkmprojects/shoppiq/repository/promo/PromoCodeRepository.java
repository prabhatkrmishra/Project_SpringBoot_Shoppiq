package com.pkmprojects.shoppiq.repository.promo;

import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for the {@link PromoCode} aggregate.
 *
 * <p>Provides methods to query promo codes by code string, check existence, and perform atomic
 * usage count increments for promo code management. The repository supports case-insensitive
 * code lookups and atomic operations to prevent race conditions during concurrent promo code
 * applications.</p>
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
