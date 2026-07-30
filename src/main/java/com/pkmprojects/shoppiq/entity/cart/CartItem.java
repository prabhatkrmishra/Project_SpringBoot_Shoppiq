package com.pkmprojects.shoppiq.entity.cart;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Represents a single line item inside a {@link Cart}.
 *
 * <p>Links one {@link ItemDetails} to a {@link Cart} with a specified
 * quantity. A unique constraint on {@code (cart_id, item_details_id)}
 * prevents the same product from appearing twice in the same cart; the
 * service layer merges duplicate adds by increasing the quantity of the
 * existing line item rather than creating a new row.</p>
 *
 * <p>The {@link ItemDetails} reference is used directly (rather than the
 * parent {@code Item}) because it holds the pricing and stock information
 * required during cart operations and checkout calculations. This entity
 * is not auditable since cart contents are transient and frequently
 * mutated.</p>
 *
 * @author prabhatkrmishra
 * @see Cart
 * @see ItemDetails
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
     * Unique identifier for this cart line item.
     *
     * <p>Generated automatically by the database using the IDENTITY
     * strategy. This primary key uniquely identifies each line item
     * within the cart and is used for item-level operations such as
     * quantity updates or removal.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cart that owns this line item.
     *
     * <p>Required relationship. Each cart item belongs to exactly one
     * cart. The cart reference is lazily loaded and maintained via the
     * {@code Cart.addItem()} helper to ensure bidirectional consistency.
     * Cascade behavior is managed by the owning {@link Cart} entity.</p>
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
     * <p>{@link ItemDetails} is used directly because it holds the
     * pricing, stock, and SKU information required during cart
     * operations, stock validation, and checkout calculations. The
     * reference is lazily loaded to avoid unnecessary joins when
     * displaying cart summaries.</p>
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
     * Number of units of the product in the cart.
     *
     * <p>Must be at least 1, enforced by the {@code @Min(1)}
     * validation constraint. When a customer attempts to add a product
     * that already exists in the cart, the service layer increments
     * this quantity rather than creating a duplicate line item. The
     * quantity is validated against available stock at checkout time.</p>
     */
    @Min(1)
    @Column(nullable = false)
    private int quantity;
}
