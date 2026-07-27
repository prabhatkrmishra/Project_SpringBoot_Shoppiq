package com.pkmprojects.shoppiq.util;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PriceUtil}.
 *
 * @author PrabhatKrMishra
 * @since 1.1.0
 */
@DisplayName("PriceUtil Tests")
class PriceUtilTest {

    private ItemDetails buildDetails(BigDecimal price, BigDecimal discountPct) {
        ItemDetails details = ItemDetails.builder()
                .brand("Brand").sku("SKU-001")
                .price(price)
                .discountPercentage(discountPct)
                .stockQuantity(10)
                .build();
        Item item = Item.builder().name("Product").description("desc").build();
        details.setItem(item);
        item.setItemDetails(details);
        return details;
    }

    @Test
    @DisplayName("Zero discount returns price unchanged at scale 2")
    void zeroDiscount_returnsOriginalPrice() {
        ItemDetails details = buildDetails(new BigDecimal("299.99"), BigDecimal.ZERO);
        assertThat(PriceUtil.effectivePrice(details))
                .isEqualByComparingTo("299.99");
    }

    @Test
    @DisplayName("Null discount percentage returns price unchanged")
    void nullDiscount_returnsOriginalPrice() {
        ItemDetails details = buildDetails(new BigDecimal("50.00"), null);
        assertThat(PriceUtil.effectivePrice(details))
                .isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("25% discount on 200 returns 150.00")
    void twentyFivePercentDiscount() {
        ItemDetails details = buildDetails(new BigDecimal("200.00"), new BigDecimal("25.00"));
        assertThat(PriceUtil.effectivePrice(details))
                .isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("10% discount on 100 returns 90.00")
    void tenPercentDiscount() {
        ItemDetails details = buildDetails(new BigDecimal("100.00"), new BigDecimal("10.00"));
        assertThat(PriceUtil.effectivePrice(details))
                .isEqualByComparingTo("90.00");
    }

    @Test
    @DisplayName("100% discount returns 0.00")
    void fullDiscount_returnsZero() {
        ItemDetails details = buildDetails(new BigDecimal("150.00"), new BigDecimal("100.00"));
        assertThat(PriceUtil.effectivePrice(details))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Rounding uses HALF_UP (33.33% of 10 = 6.67)")
    void roundingHalfUp() {
        ItemDetails details = buildDetails(new BigDecimal("10.00"), new BigDecimal("33.33"));
        assertThat(PriceUtil.effectivePrice(details))
                .isEqualByComparingTo("6.67");
    }

    @Test
    @DisplayName("Result is always scaled to 2 decimal places")
    void resultScaleIsTwo() {
        ItemDetails details = buildDetails(new BigDecimal("100.00"), new BigDecimal("15.00"));
        assertThat(PriceUtil.effectivePrice(details).scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fractional discount produces exact result")
    void fractionalDiscount() {
        // price=99.99, discount=7.5% → 99.99 * 0.925 = 92.49075 → 92.49
        ItemDetails details = buildDetails(new BigDecimal("99.99"), new BigDecimal("7.50"));
        assertThat(PriceUtil.effectivePrice(details))
                .isEqualByComparingTo("92.49");
    }

    @Test
    @DisplayName("Unscaled price input (scale 0) still returns scale 2")
    void unscaledPriceInput_returnsScaleTwo() {
        // new BigDecimal("100") has scale 0; result must be 100.00 (scale 2)
        ItemDetails details = buildDetails(new BigDecimal("100"), null);
        BigDecimal result = PriceUtil.effectivePrice(details);
        assertThat(result).isEqualByComparingTo("100.00");
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Unscaled price with discount still returns scale 2")
    void unscaledPriceWithDiscount_returnsScaleTwo() {
        // price=200 (scale 0), discount=10% → 180.00
        ItemDetails details = buildDetails(new BigDecimal("200"), new BigDecimal("10"));
        BigDecimal result = PriceUtil.effectivePrice(details);
        assertThat(result).isEqualByComparingTo("180.00");
        assertThat(result.scale()).isEqualTo(2);
    }
}
