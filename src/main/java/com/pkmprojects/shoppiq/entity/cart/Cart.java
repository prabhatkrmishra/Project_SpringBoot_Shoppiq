package com.pkmprojects.shoppiq.entity.cart;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a persistent shopping cart owned by a single {@link User}.
 *
 * <p>Each user has at most one cart, enforced by a unique constraint on
 * the {@code user_id} foreign key. The cart acts as a staging area for
 * items before checkout, with cart items stored in a separate
 * {@link CartItem} entity to support quantity tracking and unique product
 * constraints. The cart is created lazily when the user first adds an
 * item and persists across sessions.</p>
 *
 * <p>The cart lifecycle is tied to the owning user: deleting a user
 * cascade-removes their cart and all associated cart items. The cart
 * is excluded from JSON serialization in user-centric API responses to
 * prevent unnecessary data transfer.</p>
 *
 * @author prabhatkrmishra
 * @see CartItem
 * @see User
 * @since 1.0.0
 */
@Entity
@Table(
        name = "cart",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_user",
                        columnNames = "user_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cart extends AuditableEntity {

    /**
     * User who owns this cart.
     *
     * <p>One-to-one relationship: each user has exactly one cart. The
     * {@code user_id} column carries a unique constraint to enforce
     * this invariant at the database level. The user reference is
     * lazily loaded to avoid unnecessary joins when manipulating
     * cart items.</p>
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_cart_user")
    )
    private User user;

    /**
     * Items currently in the cart, each linking a product to a
     * quantity for potential purchase.
     *
     * <p>Managed via a one-to-many relationship with cascade all and
     * orphan removal. Adding or removing items should be done through
     * the {@link #addItem(CartItem)} or {@link #removeItem(CartItem)}
     * helper methods to maintain bidirectional consistency. The
     * collection defaults to an empty list for newly created carts.</p>
     */
    @Builder.Default
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items = new ArrayList<>();

    /**
     * Adds an item to the cart.
     *
     * <p>Maintains both sides of the bidirectional relationship.</p>
     *
     * @param cartItem item to add
     */
    public void addItem(CartItem cartItem) {
        if (cartItem == null) return;
        items.add(cartItem);
        cartItem.setCart(this);
    }

    /**
     * Removes an item from the cart.
     *
     * <p>Maintains both sides of the bidirectional relationship.</p>
     *
     * @param cartItem item to remove
     */
    public void removeItem(CartItem cartItem) {
        if (cartItem == null) return;
        items.remove(cartItem);
        cartItem.setCart(null);
    }
}
