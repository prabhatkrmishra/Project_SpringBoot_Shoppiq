package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a confirmed customer order using the snapshot model.
 *
 * <p>
 * Product name and price are snapshotted at purchase time inside
 * {@link OrderItem} so that historical orders remain accurate even
 * if products change later.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Order extends AuditableEntity {

    /**
     * Customer who placed the order.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_user")
    )
    private User user;

    /**
     * Shipping address selected at checkout (nullable — deleted by user after order).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "address_id",
            foreignKey = @ForeignKey(name = "fk_orders_address")
    )
    private Address address;

    /**
     * Snapshot of shipping address captured at checkout time.
     *
     * <p>Preserves the address as it was when the order was placed,
     * independent of any later edits or deletions in the address book.</p>
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullName",  column = @Column(name = "shipping_full_name")),
            @AttributeOverride(name = "phone",     column = @Column(name = "shipping_phone")),
            @AttributeOverride(name = "line1",     column = @Column(name = "shipping_line1")),
            @AttributeOverride(name = "line2",     column = @Column(name = "shipping_line2")),
            @AttributeOverride(name = "city",      column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "state",     column = @Column(name = "shipping_state")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code")),
            @AttributeOverride(name = "country",   column = @Column(name = "shipping_country"))
    })
    private OrderAddressSnapshot shippingAddress;

    /**
     * Current order lifecycle status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    /**
     * Payment method chosen by the customer.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * Current payment status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    /**
     * Sum of (unit_price × quantity) for all order items.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Shipping fee applied at checkout.
     */
    @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;

    /**
     * Tax applied at checkout.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    /**
     * Discount applied at checkout.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    /**
     * Final amount payable: subtotal + shippingFee + tax - discount.
     */
    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    /**
     * Timestamp when the order was placed.
     */
    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    /**
     * Line items that belong to this order.
     */
    @Builder.Default
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * Adds an order item and maintains bidirectional relationship.
     */
    public void addOrderItem(OrderItem orderItem) {
        if (orderItem == null) return;
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
