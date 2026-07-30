package com.pkmprojects.shoppiq.dto.item;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO used for creating and updating products in the catalog.
 *
 * <p>This record carries all fields necessary to define a product,
 * combining general product information (name, description, brand)
 * with commercial details (pricing, stock, discount) and category
 * assignment. It is submitted to the product management endpoints
 * and validated using Jakarta Bean Validation before reaching the
 * service layer.</p>
 *
 * <p>Unlike the admin-specific {@link com.pkmprojects.shoppiq.dto.admin.request.AdminItemRequest},
 * this DTO does not include a {@code sellerId} field because the
 * seller identity is derived from the authenticated user's security
 * context. It is used for both create and update operations, with
 * the service layer handling the distinction based on the presence
 * or absence of an identifier in the URL path.</p>
 *
 * @param name               product display name, required, max 150 characters;
 *                           used in storefront listings and search results
 * @param description        product description text, required, max 500
 *                           characters; displayed on the product detail page
 * @param brand              product manufacturer or brand name, required, max 100
 *                           characters; used for brand-based filtering
 * @param sku                Stock Keeping Unit identifier, required, max 100
 *                           characters; must be unique across the seller's catalog
 * @param price              unit selling price, required, non-negative;
 *                           supports up to 10 integer digits and 2 decimal places
 * @param stockQuantity      available inventory count, required, non-negative;
 *                           decremented automatically upon order placement
 * @param discountPercentage discount percentage applied to the base price,
 *                           required, between 0.00 and 100.00;
 *                           a value of 0 means no discount
 * @param imageUrl           URL of the product's primary image, optional,
 *                           max 500 characters; must be a valid URL format
 * @param categoryId         identifier of the product category, required;
 *                           must reference an existing {@code Category} entity
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ItemRequest(

        /**
         * Item name. Must not be blank.
         */
        @NotBlank(message = "Item name is required.")
        @Size(max = 150, message = "Item name cannot exceed 150 characters.")
        String name,

        /**
         * Product description. Must not be blank.
         */
        @NotBlank(message = "Description is required.")
        @Size(max = 500, message = "Description cannot exceed 500 characters.")
        String description,

        /**
         * Product brand. Must not be blank.
         */
        @NotBlank(message = "Brand is required.")
        @Size(max = 100, message = "Brand cannot exceed 100 characters.")
        String brand,

        /**
         * Stock Keeping Unit. Must not be blank.
         */
        @NotBlank(message = "SKU is required.")
        @Size(max = 100, message = "SKU cannot exceed 100 characters.")
        String sku,

        /**
         * Selling price. Must be non-negative.
         */
        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.00", message = "Price cannot be negative.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        /**
         * Available inventory. Must be zero or positive.
         */
        @NotNull(message = "Stock quantity is required.")
        @PositiveOrZero(message = "Stock quantity cannot be negative.")
        Integer stockQuantity,

        /**
         * Product discount percentage. Must be between 0 and 100.
         */
        @NotNull(message = "Discount percentage is required.")
        @DecimalMin(value = "0.00", message = "Discount cannot be negative.")
        @DecimalMax(value = "100.00", message = "Discount cannot exceed 100.")
        @Digits(integer = 3, fraction = 2)
        BigDecimal discountPercentage,

        /**
         * Product image URL. Optional.
         */
        @Size(max = 500, message = "Image URL cannot exceed 500 characters.")
        String imageUrl,

        /**
         * Category identifier. Must reference an existing category.
         */
        @NotNull(message = "Category is required.")
        Long categoryId
) {
}
