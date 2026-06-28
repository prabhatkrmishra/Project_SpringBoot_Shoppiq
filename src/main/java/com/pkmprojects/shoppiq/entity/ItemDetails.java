package com.pkmprojects.shoppiq.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents the commercial and inventory information associated with an
 * {@link Item}.
 *
 * <p>
 * This entity stores business information that changes more frequently than
 * the core product information contained in {@link Item}. It encapsulates
 * pricing, inventory, SKU management, manufacturer details and category
 * classification while allowing the parent entity to focus on catalog
 * information.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores manufacturer information.</li>
 *     <li>Stores pricing information.</li>
 *     <li>Stores inventory information.</li>
 *     <li>Stores the unique Stock Keeping Unit (SKU).</li>
 *     <li>Associates a product with a {@link Category}.</li>
 * </ul>
 *
 * <h2>Relationships</h2>
 * <ul>
 *     <li>Belongs to exactly one {@link Category}.</li>
 *     <li>Is owned by exactly one {@link Item}.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} to inherit persistence identity,
 *     optimistic locking and auditing support.</li>
 *     <li>Represents commercial information only.</li>
 *     <li>Category normalization is enforced through a foreign-key
 *     relationship rather than storing raw text.</li>
 *     <li>SKU uniqueness is enforced at both the application and database
 *     levels.</li>
 * </ul>
 *
 * <h2>Database Mapping</h2>
 * <ul>
 *     <li>Mapped to the {@code item_details} table.</li>
 *     <li>SKU is globally unique.</li>
 *     <li>Category is referenced using a foreign key.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(
        name = "item_details",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_item_details_sku",
                        columnNames = "sku"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemDetails extends AuditableEntity {

    /**
     * Manufacturer or brand of the product.
     */
    @NotBlank(message = "Brand is required.")
    @Size(max = 100, message = "Brand cannot exceed 100 characters.")
    @Column(nullable = false, length = 100)
    private String brand;

    /**
     * Stock Keeping Unit (SKU).
     *
     * <p>
     * The SKU uniquely identifies a sellable product and is primarily used
     * by inventory, warehouse and order management systems.
     * </p>
     */
    @NotBlank(message = "SKU is required.")
    @Size(max = 100, message = "SKU cannot exceed 100 characters.")
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    /**
     * Current selling price.
     */
    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Current available inventory.
     */
    @NotNull(message = "Stock quantity is required.")
    @PositiveOrZero(message = "Stock quantity cannot be negative.")
    @Builder.Default
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    /**
     * Discount percentage applied to the product.
     *
     * <p>
     * Valid values range from {@code 0.00} to {@code 100.00}.
     * </p>
     */
    @NotNull(message = "Discount percentage is required.")
    @PositiveOrZero(message = "Discount percentage cannot be negative.")
    @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100%.")
    @Digits(integer = 3, fraction = 2)
    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    /**
     * Product category.
     */
    @NotNull(message = "Category is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_item_details_category"
            )
    )
    private Category category;

    /**
     * Product that owns this commercial information.
     *
     * <p>
     * This is the inverse side of the one-to-one relationship.
     * Relationship ownership is maintained by {@link Item}.
     * </p>
     */
    @JsonIgnore
    @OneToOne(mappedBy = "itemDetails", fetch = FetchType.LAZY)
    private Item item;

    /**
     * Updates the mutable business information using the supplied source.
     *
     * <p>
     * Entity identity, optimistic locking information and audit metadata are
     * intentionally preserved. The owning {@link Item} association is also
     * preserved because this entity is the inverse side of the relationship.
     * </p>
     *
     * @param source source containing updated values
     */
    public void update(ItemDetails source) {

        if (source == null) {
            return;
        }

        this.brand = source.getBrand();
        this.sku = source.getSku();
        this.price = source.getPrice();
        this.stockQuantity = source.getStockQuantity();
        this.discountPercentage = source.getDiscountPercentage();
        this.category = source.getCategory();
    }
}