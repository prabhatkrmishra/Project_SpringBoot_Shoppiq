package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a product item via the admin panel.
 *
 * <p>This record carries all fields necessary to define a new product
 * in the catalog, including commercial information (pricing, stock,
 * discount), categorization, and optional media. It is submitted to
 * the admin item management endpoints and is validated using Jakarta
 * Bean Validation before reaching the service layer.</p>
 *
 * <p>Unlike the seller-facing {@link com.pkmprojects.shoppiq.dto.item.ItemRequest},
 * this DTO includes an explicit {@code sellerId} field so that
 * administrators can assign products to any seller. It is used for
 * both single-item creation and as the element type within
 * {@link BulkAdminItemRequest} for batch imports.</p>
 *
 * @param name               product display name, required, max 150 characters;
 *                           used in storefront listings and search results
 * @param description        product description, required, max 500 characters;
 *                           displayed on the product detail page
 * @param brand              product manufacturer or brand name, required, max 100
 *                           characters; used for brand-based filtering
 * @param sku                Stock Keeping Unit identifier, required, max 100
 *                           characters; must be unique across the entire catalog
 * @param price              unit selling price, required, non-negative;
 *                           supports up to 10 integer digits and 2 decimal places
 * @param stockQuantity      initial inventory level, required, non-negative;
 *                           decremented automatically upon order placement
 * @param discountPercentage discount percentage applied to the base price,
 *                           required, between 0.00 and 100.00;
 *                           a value of 0 means no discount
 * @param imageUrl           URL of the product's primary image, optional,
 *                           max 500 characters; must be a valid URL format
 * @param categoryId         identifier of the product category, required;
 *                           must reference an existing {@code Category} entity
 * @param sellerId           identifier of the seller who owns this product,
 *                           required; must reference an existing {@code Seller} entity
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminItemRequest(

        /**
         * Product name. Must not be blank. Max 150 characters.
         */
        @NotBlank(message = "Item name is required.")
        @Size(max = 150, message = "Item name cannot exceed 150 characters.")
        String name,

        /**
         * Product description. Must not be blank. Max 500 characters.
         */
        @NotBlank(message = "Description is required.")
        @Size(max = 500, message = "Description cannot exceed 500 characters.")
        String description,

        /**
         * Product brand. Must not be blank. Max 100 characters.
         */
        @NotBlank(message = "Brand is required.")
        @Size(max = 100, message = "Brand cannot exceed 100 characters.")
        String brand,

        /**
         * Stock Keeping Unit. Must not be blank. Max 100 characters.
         */
        @NotBlank(message = "SKU is required.")
        @Size(max = 100, message = "SKU cannot exceed 100 characters.")
        String sku,

        /**
         * Unit price. Must be non-negative.
         */
        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.00", message = "Price cannot be negative.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        /**
         * Initial inventory level. Must be zero or positive.
         */
        @NotNull(message = "Stock quantity is required.")
        @PositiveOrZero(message = "Stock quantity cannot be negative.")
        Integer stockQuantity,

        /**
         * Discount percentage. Must be between 0 and 100.
         */
        @NotNull(message = "Discount percentage is required.")
        @DecimalMin(value = "0.00", message = "Discount cannot be negative.")
        @DecimalMax(value = "100.00", message = "Discount cannot exceed 100.")
        @Digits(integer = 3, fraction = 2)
        BigDecimal discountPercentage,

        /**
         * Product image URL. Optional. Max 500 characters.
         */
        @Size(max = 500, message = "Image URL cannot exceed 500 characters.")
        String imageUrl,

        /**
         * Category identifier. Must reference an existing category.
         */
        @NotNull(message = "Category is required.")
        Long categoryId,

        /**
         * Seller identifier. Must reference an existing seller.
         */
        @NotNull(message = "Seller is required.")
        Long sellerId
) {
}
