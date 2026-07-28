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
 * <strong>Spring Boot Concept:</strong> Represents a customer review for an {@link Item}.
 *
 * <p>
 * Each review is submitted by a single {@link User} for a single
 * {@link Item}. A review contains a rating together with optional
 * textual feedback describing the customer's experience.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores customer ratings.</li>
 *     <li>Stores written product feedback.</li>
 *     <li>Associates a review with its author.</li>
 *     <li>Associates a review with the reviewed product.</li>
 * </ul>
 *
 * <h2>Relationships</h2>
 * <ul>
 *     <li>Many reviews belong to one {@link Item}.</li>
 *     <li>Many reviews may be written by one {@link User}.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} to inherit identity,
 *     optimistic locking and audit timestamps.</li>
 *     <li>The review text is optional.</li>
 *     <li>Ratings are restricted to values between 1 and 5.</li>
 * </ul>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Bidirectional {@code @ManyToOne} on both sides</strong>
 *         — ItemReview has FKs to both {@link Item} and {@link User},
 *         making it the owning side of both relationships. The inverse
 *         sides are the {@code List<ItemReview>} collections in Item and
 *         User.</li>
 *     <li><strong>{@code @JsonBackReference} / {@code @JsonIgnore}</strong>
 *         — Prevents infinite JSON serialization recursion. The Item
 *         reference uses {@code @JsonBackReference} (paired with
 *         {@code @JsonManagedReference} on the Item side), while the User
 *         reference is simply {@code @JsonIgnore}d (never exposed in API
 *         responses).</li>
 *     <li><strong>{@code @Min(1)} / {@code @Max(5)}</strong> — Bean
 *         Validation limits the rating to a 1–5 scale.</li>
 *     <li><strong>{@code @Enumerated(EnumType.STRING)} for moderation
 *         status</strong> — {@link ReviewStatus} tracks whether the review
 *         is {@code PENDING}, {@code APPROVED}, or {@code REJECTED},
 *         enabling admin moderation workflow.</li>
 *     <li><strong>{@code update()} method</strong> — Allows updating rating
 *         and text while preserving identity, version, and relationships.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
     * Rating assigned by the customer.
     *
     * <p>
     * Valid values range from 1 (worst) to 5 (best).
     * </p>
     */
    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating cannot exceed 5.")
    @Column(nullable = false)
    private Integer rating;

    /**
     * Optional written review.
     */
    @Size(max = 1000, message = "Review cannot exceed 1000 characters.")
    @Column(length = 1000)
    private String review;

    /**
     * Moderation status of this review.
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
