package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.PromoCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link PromoCodeUsage} persistence operations.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {

    /**
     * Checks whether a promo code has already been applied to a specific order.
     *
     * @param orderId the order ID
     * @return true if a usage record exists for this order
     */
    boolean existsByOrderId(Long orderId);

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
