package com.pkmprojects.shoppiq.util;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stateless utility for computing effective (post-discount) prices.
 *
 * <p>Provides methods to calculate the final selling price of an item
 * after applying percentage-based discounts. The utility ensures consistent
 * price calculations across the application by using a single rounding
 * strategy (HALF_UP to 2 decimal places).</p>
 *
 * <p>This utility is used by cart, checkout, and order services to compute
 * line item totals, order subtotals, and discount amounts. All calculations
 * use {@link BigDecimal} for precise monetary arithmetic.</p>
 *
 * @author prabhatkrmishra
 * @since 1.1.0
 */
public final class PriceUtil {

    /**
     * Prevents instantiation of this utility class.
     */
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
