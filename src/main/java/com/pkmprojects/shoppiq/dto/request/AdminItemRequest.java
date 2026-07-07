package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

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

        @NotNull(message = "Category is required.")
        Long categoryId,

        @NotNull(message = "Seller is required.")
        Long sellerId
) {
}
