package com.pkmprojects.shoppiq.entity.order;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.enums.DeliveryType;
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
 * <strong>Spring Boot Concept:</strong> Represents a confirmed customer order using the snapshot model.
 *
 * <p>
 * Product name and price are snapshotted at purchase time inside
 * {@link OrderItem} so that historical orders remain accurate even
 * if products change later.
 * </p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Snapshot pattern</strong> — {@link OrderItem} stores
 *         {@code itemNameSnapshot} and {@code unitPriceSnapshot} copied from
 *         the catalog at checkout time. The shipping address is also
 *         snapshotted via {@link OrderAddressSnapshot}. This ensures
 *         historical orders remain valid even if products or addresses are
 *         later edited or deleted.</li>
 *     <li><strong>{@code @Embedded} + {@code @AttributeOverrides}</strong>
 *         — An embeddable object ({@code OrderAddressSnapshot}) is inlined
 *         into the {@code orders} table with custom column name prefixes
 *         ({@code shipping_*}) via {@code @AttributeOverrides}.</li>
 *     <li><strong>Denormalized promo code snapshot</strong> — The
 *         {@code promoCodeSnapshot} string is stored directly on the order,
 *         in addition to the FK to {@link PromoCode}. This provides a
 *         human-readable value for display without requiring a JOIN on every
 *         order history query.</li>
 *     <li><strong>Monetary value precision</strong> — All financial fields
 *         ({@code subtotal}, {@code grandTotal}, etc.) use
 *         {@code BigDecimal} with explicit {@code precision} and
 *         {@code scale} in {@code @Column} to avoid floating-point rounding
 *         errors.</li>
 *     <li><strong>Enums for status tracking</strong> — {@link OrderStatus},
 *         {@link PaymentStatus}, {@link DeliveryType}, and
 *         {@link PaymentMethod} are all stored as {@code @Enumerated(STRING)}
 *         for readability in the database.</li>
 *     <li><strong>{@code @OneToMany(cascade = ALL, orphanRemoval = true)}</strong>
 *         — OrderItems are children of the Order; cascade ensures they are
 *         persisted/removed with the parent.</li>
 *     <li><strong>{@code addOrderItem()} helper</strong> — Maintains the
 *         bidirectional relationship consistency.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
     * Delivery speed selected at checkout.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 20)
    @Builder.Default
    private DeliveryType deliveryType = DeliveryType.NORMAL;

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
     * Delivery charge based on delivery type.
     * {@code 7.50} for EXPRESS_1DAY, {@code 0} for NORMAL.
     */
    @Column(name = "delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge;

    /**
     * Cash-on-delivery surcharge.
     * {@code 5.00} when payment method is COD, {@code 0} otherwise.
     */
    @Column(name = "cod_surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal codSurcharge;

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
     * Final amount payable: subtotal + deliveryCharge + codSurcharge + tax - discount.
     */
    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    /**
     * Promo code applied at checkout, if any.
     *
     * <p>Snapshot reference preserved for historical orders.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "promo_code_id",
            foreignKey = @ForeignKey(name = "fk_orders_promo_code")
    )
    private PromoCode promoCode;

    /**
     * Denormalized promo code string captured at checkout time.
     *
     * <p>Preserves the code even if the original {@link PromoCode} entity
     * is later deleted or deactivated. Used by {@code CheckoutResponse}
     * and order history displays to show which promo was applied without
     * requiring a lazy-loaded join to the {@code promo_codes} table.</p>
     *
     * <p>Populated by {@code CheckoutServiceImpl} when a promo code is
     * applied. {@code null} when no promo was used. Backfilled for
     * existing orders by Flyway migration {@code V34}.</p>
     *
     * @see com.pkmprojects.shoppiq.dto.order.CheckoutResponse#from
     */
    @Column(name = "promo_code_snapshot", length = 50)
    private String promoCodeSnapshot;

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
