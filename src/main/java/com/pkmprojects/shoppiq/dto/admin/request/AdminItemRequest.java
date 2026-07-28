package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a product item via the admin panel.
 *
 * <p>Carries all fields necessary to define a new product including
 * pricing, stock, categorization, and media. Each field is validated
 * with Bean Validation constraints suitable for admin-level input.</p>
 *
 * <p><b>Validation patterns demonstrated:</b></p>
 * <ul>
 *   <li>{@code @NotBlank + @Size} — string length and required checks</li>
 *   <li>{@code @DecimalMin / @DecimalMax} — numeric range validation for prices and percentages</li>
 *   <li>{@code @Digits(integer=10, fraction=2)} — ensures precise decimal formatting</li>
 *   <li>{@code @PositiveOrZero} — non-negative stock validation</li>
 * </ul>
 * <p>Because this is a Java record, all fields are {@code final} and the
 * canonical constructor is auto-generated — ideal for request DTOs that
 * should not be mutated after creation.</p>
 *
 * @param name             the product name (required, max 150 chars)
 * @param description      the product description (required, max 500 chars)
 * @param brand            the brand name
 * @param sku              the stock-keeping unit identifier
 * @param price            the unit price (required, positive, max 8 digits)
 * @param stockQuantity    the initial inventory level
 * @param discountPercentage the discount percentage (0–100)
 * @param imageUrl         the URL for the product image
 * @param categoryId       the identifier of the category to assign
 * @param sellerId         the identifier of the seller who owns this product
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminItemRequest(

        @NotBlank(message = "Item name is required.")
        @Size(max = 150, message = "Item name cannot exceed 150 characters.")
        String name,

        @NotBlank(message = "Description is required.")
        @Size(max = 500, message = "Description cannot exceed 500 characters.")
        String description,

        @NotBlank(message = "Brand is required.")
        @Size(max = 100, message = "Brand cannot exceed 100 characters.")
        String brand,

        @NotBlank(message = "SKU is required.")
        @Size(max = 100, message = "SKU cannot exceed 100 characters.")
        String sku,

        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.00", message = "Price cannot be negative.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotNull(message = "Stock quantity is required.")
        @PositiveOrZero(message = "Stock quantity cannot be negative.")
        Integer stockQuantity,

        @NotNull(message = "Discount percentage is required.")
        @DecimalMin(value = "0.00", message = "Discount cannot be negative.")
        @DecimalMax(value = "100.00", message = "Discount cannot exceed 100.")
        @Digits(integer = 3, fraction = 2)
        BigDecimal discountPercentage,

        @Size(max = 500, message = "Image URL cannot exceed 500 characters.")
        String imageUrl,

        @NotNull(message = "Category is required.")
        Long categoryId,

        @NotNull(message = "Seller is required.")
        Long sellerId
) {
}
