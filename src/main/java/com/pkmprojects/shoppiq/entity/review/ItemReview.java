package com.pkmprojects.shoppiq.entity.review;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.ReviewStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Represents a customer review for an {@link Item}.
 *
 * <p>Each review is submitted by a single {@link User} for a single
 * {@link Item}. Contains a rating on a 1-5 scale together with optional
 * textual feedback describing the customer's experience. Reviews go
 * through a moderation workflow tracked by {@link ReviewStatus}
 * ({@code PENDING}, {@code APPROVED}, {@code REJECTED}) before being
 * displayed publicly on the product detail page.</p>
 *
 * <p>The user-item combination should be unique (enforced at the service
 * layer) to prevent duplicate reviews. Reviews are cascade-deleted when
 * either the owning user or the reviewed item is removed, maintaining
 * referential integrity without requiring manual cleanup. The review
 * entity supports bidirectional relationships with both User and Item
 * through helper methods that maintain consistency on both sides.</p>
 *
 * @author prabhatkrmishra
 * @see Item
 * @see User
 * @see ReviewStatus
 * @since 1.0.0
 */
@Entity
@Table(name = "item_reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemReview extends AuditableEntity {

    /**
     * Customer who submitted this review.
     *
     * <p>Required relationship. Each review is associated with exactly
     * one user. The user reference is lazily loaded and excluded from
     * JSON serialization via {@code @JsonIgnore} to prevent circular
     * reference issues in API responses. Used for review ownership
     * verification and user-specific review queries.</p>
     */
    @NotNull(message = "Reviewer is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_review_user")
    )
    @JsonIgnore
    private User user;

    /**
     * Product being reviewed.
     *
     * <p>Required relationship. Each review is associated with exactly
     * one item. The item reference is lazily loaded and uses
     * {@code @JsonBackReference} to prevent circular reference issues
     * in the bidirectional relationship with the item's review
     * collection. Used for product-level review aggregation and
     * display.</p>
     */
    @NotNull(message = "Item is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "item_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_review_item")
    )
    @JsonBackReference
    private Item item;

    /**
     * Rating assigned by the customer, ranging from 1 (worst) to
     * 5 (best).
     *
     * <p>Required field. Used to compute aggregate product ratings and
     * for filtering products by minimum rating. The rating is displayed
     * as star icons on product cards and detail pages. Validation
     * constraints enforce the valid range at the persistence layer.</p>
     */
    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating cannot exceed 5.")
    @Column(nullable = false)
    private Integer rating;

    /**
     * Optional written review providing detailed customer feedback
     * about the product experience.
     *
     * <p>Maximum length of 1000 characters. When present, displayed
     * below the rating on the product detail page after moderation
     * approval. May be {@code null} for ratings-only reviews where
     * the customer provides a star rating without written commentary.</p>
     */
    @Size(max = 1000, message = "Review cannot exceed 1000 characters.")
    @Column(length = 1000)
    private String review;

    /**
     * Moderation status of this review controlling its public visibility.
     *
     * <p>Stored as a string enum with a maximum length of 20 characters.
     * Defaults to {@link ReviewStatus#PENDING} for new submissions.
     * Only {@code APPROVED} reviews are displayed on the product detail
     * page. Admins can approve or reject reviews through the moderation
     * dashboard, transitioning the status accordingly.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    /**
     * Updates the mutable fields of this review.
     *
     * <p>
     * Entity identity, auditing information and relationship ownership
     * are intentionally preserved.
     * </p>
     *
     * @param source source containing updated review information
     */
    public void update(ItemReview source) {

        if (source == null) {
            return;
        }

        this.rating = source.getRating();
        this.review = source.getReview();
    }
}
