package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link PromoCode} persistence operations.
 *
 * @author PrabhatKrMishra
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
}
