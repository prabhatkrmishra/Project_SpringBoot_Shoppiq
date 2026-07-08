package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO used for creating and updating {@link com.pkmprojects.shoppiq.entity.Item}.
 *
 * <p>
 * This DTO represents the complete product information required by the
 * catalog management API. It combines the general product information
 * stored by {@code Item} with the commercial information stored by
 * {@code ItemDetails}.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Accept client supplied product information.</li>
 *     <li>Perform request validation.</li>
 *     <li>Remain independent of persistence entities.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Only identifiers are accepted for relationships.</li>
 *     <li>Category is referenced using its identifier instead of embedding
 *     an entire entity.</li>
 *     <li>Used for both create and update operations.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ItemRequest(

        /*
          Product name.
         */
        @NotBlank(message = "Item name is required.")
        @Size(max = 150, message = "Item name cannot exceed 150 characters.")
        String name,

        /*
          Product description.
         */
        @NotBlank(message = "Description is required.")
        @Size(max = 500, message = "Description cannot exceed 500 characters.")
        String description,

        /*
          Product manufacturer.
         */
        @NotBlank(message = "Brand is required.")
        @Size(max = 100, message = "Brand cannot exceed 100 characters.")
        String brand,

        /*
          Stock Keeping Unit.
         */
        @NotBlank(message = "SKU is required.")
        @Size(max = 100, message = "SKU cannot exceed 100 characters.")
        String sku,

        /*
          Selling price.
         */
        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.00", message = "Price cannot be negative.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        /*
          Available inventory.
         */
        @NotNull(message = "Stock quantity is required.")
        @PositiveOrZero(message = "Stock quantity cannot be negative.")
        Integer stockQuantity,

        /*
          Product discount percentage.
         */
        @NotNull(message = "Discount percentage is required.")
        @DecimalMin(value = "0.00", message = "Discount cannot be negative.")
        @DecimalMax(value = "100.00", message = "Discount cannot exceed 100.")
        @Digits(integer = 3, fraction = 2)
        BigDecimal discountPercentage,

        /*
          Product image URL.
         */
        @Size(max = 500, message = "Image URL cannot exceed 500 characters.")
        String imageUrl,

        /*
          Category identifier.
         */
        @NotNull(message = "Category is required.")
        Long categoryId
) {
}