package com.pkmprojects.shoppiq.dto.response;

import com.pkmprojects.shoppiq.entity.Item;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO returned for {@link Item} resources.
 *
 * <p>
 * This DTO represents the complete product information exposed by the
 * catalog REST API. It combines general product information stored by
 * {@link Item} with commercial information stored by
 * {@link com.pkmprojects.shoppiq.entity.ItemDetails}.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose product information to API consumers.</li>
 *     <li>Hide internal JPA entities.</li>
 *     <li>Provide a stable API contract.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Contains a nested {@link CategoryResponse} instead of exposing
 *     the Category entity.</li>
 *     <li>Immutable through Java Records.</li>
 *     <li>Created using {@link #fromEntity(Item)}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ItemResponse(

        /*
          Unique product identifier.
         */
        Long id,

        /*
          Product name.
         */
        String name,

        /*
          Product description.
         */
        String description,

        /*
          Product manufacturer.
         */
        String brand,

        /*
          Stock Keeping Unit.
         */
        String sku,

        /*
          Current selling price.
         */
        BigDecimal price,

        /*
          Current inventory.
         */
        Integer stockQuantity,

        /*
          Product discount percentage.
         */
        BigDecimal discountPercentage,

        /*
          Product category.
         */
        CategoryResponse category,

        /*
          Creation timestamp.
         */
        Instant createdAt,

        /*
          Last modification timestamp.
         */
        Instant updatedAt

) {

    /**
     * Creates an {@code ItemResponse} from an {@link Item} entity.
     *
     * <p>
     * This factory method centralizes the mapping logic between the
     * persistence layer and the API layer.
     * </p>
     *
     * @param item item entity
     * @return mapped response DTO
     */
    public static ItemResponse fromEntity(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getItemDetails().getBrand(),
                item.getItemDetails().getSku(),
                item.getItemDetails().getPrice(),
                item.getItemDetails().getStockQuantity(),
                item.getItemDetails().getDiscountPercentage(),

                CategoryResponse.fromEntity(
                        item.getItemDetails().getCategory()
                ),

                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}