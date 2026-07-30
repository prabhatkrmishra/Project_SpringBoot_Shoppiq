package com.pkmprojects.shoppiq.entity.item;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pkmprojects.shoppiq.aiservice.events.ItemEmbeddingEntityListener;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product available in the Shoppiq catalog.
 *
 * <p>Contains general catalog information (name, description, slug) displayed
 * to customers. Detailed commercial information such as pricing, inventory,
 * SKU and category is delegated to {@link ItemDetails}. Each item belongs to
 * a {@link Seller} and may collect customer reviews. The separation between
 * {@code Item} and {@code ItemDetails} reflects a design decision to isolate
 * frequently-read catalog data from commerce data that changes more often.</p>
 *
 * <p>Product publishing status controls visibility: new products start as
 * {@code DRAFT} and must be published by an admin before customers can
 * see them. The {@code ItemEmbeddingEntityListener} triggers AI-based
 * embedding generation for semantic search capabilities whenever the
 * product is persisted or updated.</p>
 *
 * @author prabhatkrmishra
 * @see ItemDetails
 * @see Seller
 * @see ItemReview
 * @since 1.0.0
 */
@Entity
@EntityListeners(ItemEmbeddingEntityListener.class)
@Table(name = "items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Item extends AuditableEntity {

    /**
     * Product name displayed throughout the catalog, search results,
     * and product detail pages.
     *
     * <p>Required field with a maximum length of 150 characters. This
     * is the primary human-readable identifier for the product and is
     * used in product listings, cart views, and order summaries.</p>
     */
    @NotBlank(message = "Item name is required.")
    @Size(max = 150, message = "Item name cannot exceed 150 characters.")
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * URL-friendly slug derived from the product name, used for
     * SEO-optimized product detail page routing.
     *
     * <p>Required field with a maximum length of 200 characters. Must
     * be globally unique across the entire catalog. The slug appears in
     * URLs such as {@code /item/wireless-bluetooth-headphones}. Generated
     * by the service layer to ensure consistency and collision-free
     * naming.</p>
     */
    @NotBlank(message = "Item slug is required.")
    @Size(max = 200, message = "Item slug cannot exceed 200 characters.")
    @Column(nullable = false, length = 200, unique = true)
    private String slug;

    /**
     * Short product description summarizing the key features and
     * benefits for the customer.
     *
     * <p>Required field with a maximum length of 500 characters. Displayed
     * on product listing cards and in search results. For longer,
     * detailed descriptions, use the rich-text content in
     * {@link ItemDetails}.</p>
     */
    @NotBlank(message = "Item description is required.")
    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    @Column(nullable = false, length = 500)
    private String description;

    /**
     * The seller who owns and manages this product listing.
     *
     * <p>Required relationship. Each product belongs to exactly one seller.
     * The seller reference is lazily loaded to avoid unnecessary joins
     * when querying products without needing seller details. Used for
     * seller-specific dashboards, commission calculations, and product
     * management authorization.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "seller_id",
            foreignKey = @ForeignKey(name = "fk_items_seller")
    )
    private Seller seller;

    /**
     * Publishing status controlling the product's visibility in the
     * storefront and search results.
     *
     * <p>New products created by a seller start as {@code DRAFT} and
     * are invisible to customers. An admin must review and publish
     * them before they appear in listings. Stored as a string enum
     * with a maximum length of 20 characters for readability in
     * database queries and audit logs.</p>
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "publishing_status", nullable = false, length = 20)
    private ProductPublishingStatus publishingStatus = ProductPublishingStatus.DRAFT;

    /**
     * Commercial and inventory information associated with this product,
     * including pricing, SKU, stock levels, and category classification.
     *
     * <p>The lifecycle of {@link ItemDetails} is fully managed by the
     * owning {@code Item}. Deleting an item automatically removes its
     * details via cascade orphan removal. This is the owning side of
     * the one-to-one relationship. The details entity is validated
     * using {@code @Valid} to ensure commercial data integrity at
     * persist time.</p>
     */
    @Valid
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            optional = false
    )
    @JoinColumn(
            name = "item_details_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_items_item_details"
            )
    )
    private ItemDetails itemDetails;

    /**
     * Customer reviews submitted for this product.
     *
     * <p>A product may receive many reviews from different customers.
     * Reviews are cascade-deleted when the product is removed, and
     * orphan removal ensures cleanup of disassociated review records.
     * The collection is lazily loaded and excluded from default JSON
     * serialization via {@code @JsonManagedReference} to prevent
     * circular reference issues.</p>
     */
    @Builder.Default
    @OneToMany(
            mappedBy = "item",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<ItemReview> itemReviews = new ArrayList<>();

    /**
     * Updates the mutable state of this item using the supplied source.
     *
     * <p>
     * The entity identity, optimistic locking version and audit metadata are
     * intentionally preserved. Only business fields are copied.
     * </p>
     *
     * <p>
     * If {@link ItemDetails} already exists, its state is updated rather than
     * replacing the managed entity instance.
     * </p>
     *
     * @param source item containing updated values
     */
    public void update(Item source) {

        if (source == null) {
            throw new IllegalArgumentException("Update source must not be null.");
        }

        if (source.getName() != null) {
            this.name = source.getName();
        }
        if (source.getDescription() != null) {
            this.description = source.getDescription();
        }

        if (source.getPublishingStatus() != null) {
            this.publishingStatus = source.getPublishingStatus();
        }

        if (source.getItemDetails() != null) {

            if (this.itemDetails == null) {
                this.itemDetails = source.getItemDetails();
            } else {
                this.itemDetails.update(source.getItemDetails());
            }
        }
    }

    /**
     * Associates a review with this item.
     *
     * <p>
     * This helper maintains both sides of the bidirectional relationship,
     * ensuring the persistence context remains consistent.
     * </p>
     *
     * @param review review to associate
     */
    public void addReview(ItemReview review) {

        if (review == null) {
            return;
        }

        itemReviews.add(review);
        review.setItem(this);
    }

    /**
     * Removes a review from this item.
     *
     * <p>
     * This helper maintains both sides of the bidirectional relationship.
     * If orphan removal is enabled, the removed review will be deleted when
     * the persistence context is flushed.
     * </p>
     *
     * @param review review to remove
     */
    public void removeReview(ItemReview review) {

        if (review == null) {
            return;
        }

        itemReviews.remove(review);
        review.setItem(null);
    }
}
