package com.pkmprojects.shoppiq.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.aiservice.events.ItemEmbeddingEntityListener;
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
 * <p>
 * An {@code Item} contains the general catalog information displayed to
 * customers, while detailed commercial information such as pricing,
 * inventory, SKU and category is delegated to {@link ItemDetails}.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores product name and description.</li>
 *     <li>Owns the associated {@link ItemDetails}.</li>
 *     <li>Maintains product reviews.</li>
 *     <li>Participates in customer orders.</li>
 *     <li>Belongs to a {@link Seller} (marketplace ownership).</li>
 * </ul>
 *
 * <h2>Relationships</h2>
 * <ul>
 *     <li>One-to-One with {@link ItemDetails}.</li>
 *     <li>Many-to-One with {@link Seller}.</li>
 *     <li>One-to-Many with {@link ItemReview}.</li>
 *     <li>Referenced by {@link OrderItem} snapshots at purchase time.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} to inherit persistence,
 *     optimistic locking and auditing support.</li>
 *     <li>Uses cascading for {@link ItemDetails} because it is owned
 *     exclusively by this entity.</li>
 *     <li>Reviews are orphan-removable to preserve referential integrity.</li>
 *     <li>Seller is nullable for backward compatibility with legacy items.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * Product name displayed throughout the catalog.
     */
    @NotBlank(message = "Item name is required.")
    @Size(max = 150, message = "Item name cannot exceed 150 characters.")
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * URL-friendly slug derived from the product name.
     */
    @NotBlank(message = "Item slug is required.")
    @Size(max = 200, message = "Item slug cannot exceed 200 characters.")
    @Column(nullable = false, length = 200, unique = true)
    private String slug;

    /**
     * Short product description.
     */
    @NotBlank(message = "Item description is required.")
    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    @Column(nullable = false, length = 500)
    private String description;

    /**
     * The seller who owns this product.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "seller_id",
            foreignKey = @ForeignKey(name = "fk_items_seller")
    )
    private Seller seller;

    /**
     * Publishing status of this product.
     *
     * <p>New products created by a seller start as {@code DRAFT}.
     * An admin must publish them before they become visible to
     * customers.</p>
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "publishing_status", nullable = false, length = 20)
    private ProductPublishingStatus publishingStatus = ProductPublishingStatus.DRAFT;

    /**
     * Commercial and inventory information associated with this product.
     *
     * <p>
     * The lifecycle of {@link ItemDetails} is fully managed by the owning
     * {@code Item}. Deleting an item automatically removes its details.
     * </p>
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
     * Reviews submitted for this product.
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
            return;
        }

        this.name = source.getName();
        this.description = source.getDescription();

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