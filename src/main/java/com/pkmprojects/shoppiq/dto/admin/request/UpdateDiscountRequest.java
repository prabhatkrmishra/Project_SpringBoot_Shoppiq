package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Request DTO for updating a product's discount percentage.
 *
 * <p>This record is a single-field DTO submitted to the admin discount
 * update endpoint to set or modify the discount percentage applied to
 * a specific product. It replaces the previous raw map-based request
 * body approach, providing type safety and self-documenting API
 * contracts.</p>
 *
 * <p>The discount percentage is applied to the product's base price to
 * compute the effective selling price. A value of 0 means no discount
 * (full price). The effective price is calculated as
 * {@code basePrice * (1 - discountPercentage / 100)} and is displayed
 * in the storefront alongside the original price.</p>
 *
 * @param discountPercentage the new discount percentage to apply;
 *                           must be zero or non-negative; values
 *                           between 0 and 100 represent valid
 *                           percentages; the service layer caps
 *                           the effective price at zero to prevent
 *                           negative selling prices
 * @author prabhatkrmishra
 * @since 1.5.0
 */
public record UpdateDiscountRequest(
        /**
         * Discount percentage. Must be zero or positive.
         */
        @NotNull(message = "Discount percentage is required.")
        @PositiveOrZero(message = "Discount percentage must be zero or positive.")
        BigDecimal discountPercentage
) {
}
