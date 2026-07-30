package com.pkmprojects.shoppiq.dto.admin.request;

/**
 * Request DTO for toggling a single product's on-sale status.
 *
 * <p>This record is a single-field DTO submitted to the admin on-sale
 * toggle endpoint to enable or disable the "on sale" flag for an
 * individual product. It replaces the previous raw map-based request
 * body approach, providing type safety and self-documenting API
 * contracts.</p>
 *
 * <p>When {@code onSale} is {@code true}, the product becomes eligible
 * for promotional display in the storefront. When {@code false}, the
 * product is removed from sale-related listings. This endpoint does
 * not modify the discount percentage; use
 * {@link UpdateDiscountRequest} for that purpose.</p>
 *
 * @param onSale whether the product should be marked as on sale;
 *               {@code true} to enable, {@code false} to disable
 * @author prabhatkrmishra
 * @since 1.5.0
 */
public record ToggleOnSaleRequest(
        /**
         * Whether the product should be marked as on sale.
         */
        boolean onSale
) {
}
