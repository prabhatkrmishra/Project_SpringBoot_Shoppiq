package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for toggling the on-sale status of multiple products at once.
 *
 * <p>This record is submitted to the admin bulk on-sale toggle endpoint
 * to enable or disable the "on sale" flag for a batch of products in a
 * single API call. When {@code onSale} is {@code true}, an optional
 * {@code discountPercentage} can be specified to apply a uniform
 * discount across all listed products. When {@code onSale} is
 * {@code false}, the discount percentage is ignored and any existing
 * on-sale discount is cleared.</p>
 *
 * <p>The list of item identifiers must not be empty and each identifier
 * must be a positive number. The service layer processes the request
 * atomically, either applying the toggle to all items or rolling back
 * on failure.</p>
 *
 * @param itemIds            list of product identifiers to toggle; must not be
 *                           empty; each identifier must be a positive number
 *                           referencing an existing {@code Item} entity
 * @param onSale             whether the listed products should be marked as
 *                           on sale ({@code true}) or removed from sale ({@code false})
 * @param discountPercentage optional discount percentage to apply when
 *                           enabling on-sale; ignored when {@code onSale}
 *                           is {@code false}; must be between 0 and 100
 *                           if provided
 * @author prabhatkrmishra
 * @since 1.5.0
 */
public record BulkOnSaleRequest(

        /**
         * List of item IDs to toggle on-sale status.
         */
        @NotNull(message = "Item IDs are required.")
        List<@Positive Long> itemIds,

        /**
         * Whether the items should be marked as on sale.
         */
        boolean onSale,

        /**
         * Optional discount percentage. Ignored when {@code onSale} is {@code false}.
         */
        BigDecimal discountPercentage
) {
}
