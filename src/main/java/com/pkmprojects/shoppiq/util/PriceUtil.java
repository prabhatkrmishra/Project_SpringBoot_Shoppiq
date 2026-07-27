package com.pkmprojects.shoppiq.util;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stateless utility for price calculations shared across service layers.
 *
 * <h2>Effective price formula</h2>
 * <pre>
 *   effectivePrice = price × (1 − discountPercentage / 100)
 * </pre>
 * Rounded to 2 decimal places using {@link RoundingMode#HALF_UP}.
 *
 * @author PrabhatKrMishra
 * @since 1.1.0
 */
public final class PriceUtil {

    private PriceUtil() {
        // Stateless utility [not instantiable]
    }

    /**
     * Computes the effective (post-item-discount) price for an item.
     *
     * <p>When {@code discountPercentage} is {@code 0} or {@code null}, the
     * original price is returned at scale 2.</p>
     *
     * @param itemDetails the item details containing price and discount info
     * @return the effective selling price, rounded to 2 decimal places
     */
    public static BigDecimal effectivePrice(ItemDetails itemDetails) {
        BigDecimal discountPct = itemDetails.getDiscountPercentage();
        if (discountPct == null || discountPct.signum() == 0) {
            return itemDetails.getPrice().setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal discount = discountPct
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return itemDetails.getPrice()
                .multiply(BigDecimal.ONE.subtract(discount))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
