package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a persistent shopping cart owned by a single {@link User}.
 *
 * <p>
 * Each user has at most one cart. The cart acts as a staging area for
 * items before checkout. Cart items are stored in a separate
 * {@link CartItem} entity to support quantity tracking and unique
 * product constraints.
 * </p>
 *
 * <h2>Relationships</h2>
 * <ul>
 *     <li>One-to-One with {@link User} (owns the cart).</li>
 *     <li>One-to-Many with {@link CartItem}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * Items currently in the cart.
     *
     * <p>
     * Cart items are removed automatically when the cart is deleted.
     * </p>
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
