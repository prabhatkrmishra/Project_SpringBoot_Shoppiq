package com.pkmprojects.shoppiq.repository.promo;

import com.pkmprojects.shoppiq.entity.promo.PromoCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link PromoCodeUsage} persistence operations.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived exists query by flat field</strong> — {@code existsByOrderId} checks
 *       whether a promo code has already been applied to a specific order.</li>
 *   <li><strong>Custom JPQL count query</strong> — {@code countByPromoCodeIdAndUserId} counts
 *       per-user usage by joining across {@link com.pkmprojects.shoppiq.entity.promo.PromoCode}
 *       and {@link com.pkmprojects.shoppiq.entity.user.User} associations via their IDs.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   existsByOrderId(Long)
 *       → SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM promo_code_usages WHERE order_id = ?
 *   countByPromoCodeIdAndUserId(@Query)
 *       → SELECT COUNT(u) FROM PromoCodeUsage u WHERE u.promoCode.id = :promoCodeId AND u.user.id = :userId
 *         → SELECT COUNT(*) FROM promo_code_usages WHERE promo_code_id = ? AND user_id = ?
 * </pre>
 *
 * @author prabhatkrmishra
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
