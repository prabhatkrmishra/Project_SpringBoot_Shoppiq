package com.pkmprojects.shoppiq.entity.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents the commercial and inventory information associated with
 * an {@link Item}.
 *
 * <p>Encapsulates pricing, inventory, SKU management, manufacturer details
 * and category classification. Separates commerce-level data that changes
 * more frequently from the core catalog information in {@link Item}. This
 * separation allows catalog browsing queries to avoid loading heavyweight
 * commercial data until the customer reaches the product detail page.</p>
 *
 * <p>SKU uniqueness is enforced at both the application and database levels
 * via a unique constraint on the {@code sku} column. Category is normalized
 * via a foreign key rather than stored as raw text, ensuring referential
 * integrity and enabling category-based filtering without string matching.
 * The entity is the inverse side of a one-to-one relationship with
 * {@link Item}, which owns the mapping.</p>
 *
 * @author prabhatkrmishra
 * @see Item
 * @see com.pkmprojects.shoppiq.entity.category.Category
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
     * Manufacturer or brand name of the product (e.g. "Samsung",
     * "Nike", "Apple").
     *
     * <p>Required field with a maximum length of 100 characters. Used
     * for brand-based filtering and display on product detail pages.
     * This field is not validated for uniqueness since multiple products
     * from the same brand are expected.</p>
     */
    @NotBlank(message = "Brand is required.")
    @Size(max = 100, message = "Brand cannot exceed 100 characters.")
    @Column(nullable = false, length = 100)
    private String brand;

    /**
     * Stock Keeping Unit (SKU) used to uniquely identify a sellable
     * product across inventory, warehouse, and order management systems.
     *
     * <p>Required field with a maximum length of 100 characters. Must be
     * globally unique across the entire catalog, enforced by a database
     * unique constraint. The SKU is the primary identifier used in
     * fulfillment workflows, stock reconciliation, and reporting.</p>
     */
    @NotBlank(message = "SKU is required.")
    @Size(max = 100, message = "SKU cannot exceed 100 characters.")
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    /**
     * Current selling price of the product in the store's base currency.
     *
     * <p>Required field with a precision of 10 digits total and 2
     * decimal places. Must be non-negative (zero is allowed for free
     * promotional items). This price is snapshotted into
     * {@link com.pkmprojects.shoppiq.entity.order.OrderItem} at
     * checkout time to preserve historical accuracy.</p>
     */
    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Current available inventory count for this product.
     *
     * <p>Must be zero or positive. Decremented atomically when an order
     * is placed and restored if the order is cancelled. Products with
     * zero stock may still appear in the catalog but are marked as
     * out of stock. Defaults to 0 for newly created products.</p>
     */
    @NotNull(message = "Stock quantity is required.")
    @PositiveOrZero(message = "Stock quantity cannot be negative.")
    @Builder.Default
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    /**
     * Discount percentage applied to the product's base price.
     *
     * <p>Valid values range from {@code 0.00} (no discount) to
     * {@code 100.00} (completely free). The effective price is
     * calculated as {@code price * (1 - discountPercentage / 100)}.
     * Defaults to {@code 0.00} for new products. This field is
     * distinct from the {@code onSale} flag, which controls
     * promotional visibility rather than pricing.</p>
     */
    @NotNull(message = "Discount percentage is required.")
    @PositiveOrZero(message = "Discount percentage cannot be negative.")
    @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100%.")
    @Digits(integer = 3, fraction = 2)
    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    /**
     * URL or storage key pointing to the product's primary image asset.
     *
     * <p>Optional field with a maximum length of 500 characters. When
     * absent, the frontend displays a default placeholder image. The
     * URL may point to an external CDN, cloud storage bucket, or a
     * relative path within the application's asset pipeline.</p>
     */
    @Size(max = 500, message = "Image URL cannot exceed 500 characters.")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Whether this product is currently featured in promotional sale
     * events and appears on the Sale page.
     *
     * <p>Admin-controlled flag used to mark products for promotional
     * visibility. When {@code true}, the product is included in sale
     * listings and may receive special visual treatment (e.g. sale
     * badges, highlighted cards). Defaults to {@code false} for new
     * products.</p>
     */
    @Builder.Default
    @Column(name = "on_sale", nullable = false)
    private boolean onSale = false;

    /**
     * Product category that this item belongs to, used for catalog
     * navigation, filtering, and SEO-friendly category pages.
     *
     * <p>Required relationship. Each product must belong to exactly one
     * category. The category is lazily loaded and joined via a foreign
     * key with an explicit constraint name. Changing a product's
     * category is supported but should be done judiciously as it
     * affects URL routing and search indexing.</p>
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
     * <p>This is the inverse side of the one-to-one relationship with
     * {@link Item}. Relationship ownership and cascade behavior are
     * maintained by the owning {@link Item} entity. This reference is
     * excluded from JSON serialization via {@code @JsonIgnore} to
     * prevent circular reference issues during API responses.</p>
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
        this.imageUrl = source.getImageUrl();
        this.onSale = source.isOnSale();
        this.category = source.getCategory();
    }
}
