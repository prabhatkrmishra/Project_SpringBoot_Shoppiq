package com.pkmprojects.shoppiq.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Represents a single line item inside a {@link Cart}.
 *
 * <p>
 * Each {@code CartItem} links one {@link ItemDetails} to a {@link Cart}
 * with a specified quantity. A unique constraint on {@code (cart_id,
 * item_details_id)} prevents the same product from appearing twice;
 * the service layer merges duplicate adds by increasing the quantity.
 * </p>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Does not extend {@link com.pkmprojects.shoppiq.audit.AuditableEntity}
 *     because audit timestamps are tracked at the cart level.</li>
 *     <li>Identity uses {@link GenerationType#IDENTITY}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_items_cart_item",
                        columnNames = {"cart_id", "item_details_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CartItem {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cart that owns this line item.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cart_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_items_cart")
    )
    private Cart cart;

    /**
     * Product (via {@link ItemDetails}) added to the cart.
     *
     * <p>
     * {@link ItemDetails} is used directly because it holds pricing and
     * stock information required during cart operations and checkout.
     * </p>
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "item_details_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_items_item_details")
    )
    private ItemDetails itemDetails;

    /**
     * Number of units of the product.
     *
     * <p>Must be at least 1.</p>
     */
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;
}
