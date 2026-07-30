package com.pkmprojects.shoppiq.entity.order;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.entity.user.User;
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
 * Represents a confirmed customer order using the snapshot model.
 *
 * <p>Product name and price are snapshotted at purchase time inside
 * {@link OrderItem} so that historical orders remain accurate even if
 * products change later. The shipping address is similarly snapshotted
 * via {@link OrderAddressSnapshot}, ensuring that order history is
 * self-contained and independent of mutations to the user's address
 * book or product catalog.</p>
 *
 * <p>Tracks the full order lifecycle through {@link OrderStatus},
 * {@link PaymentStatus}, and {@link DeliveryType}. A denormalized
 * {@code promoCodeSnapshot} string preserves the applied promo code
 * for display without requiring a join on every history query. The
 * grand total is computed as the sum of subtotal, delivery charge,
 * COD surcharge, and tax, minus any applied discount.</p>
 *
 * @author prabhatkrmishra
 * @see OrderItem
 * @see OrderAddressSnapshot
 * @see com.pkmprojects.shoppiq.entity.payment.Payment
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
     *
     * <p>Required relationship. Each order is associated with exactly
     * one user account. The user reference is lazily loaded to avoid
     * unnecessary joins when processing order items or payment details.
     * Used for order history queries, customer support lookups, and
     * notification delivery.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_user")
    )
    private User user;

    /**
     * Reference to the user's shipping address at the time of checkout.
     *
     * <p>Nullable because the user may delete their address after placing
     * the order. The actual shipping details are preserved in the
     * {@link #shippingAddress} snapshot for historical accuracy. This
     * FK serves as a convenience link for active orders where the
     * address still exists.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "address_id",
            foreignKey = @ForeignKey(name = "fk_orders_address")
    )
    private Address address;

    /**
     * Embedded snapshot of shipping address fields captured at checkout
     * time, preserving the address as it was when the order was placed.
     *
     * <p>This snapshot is independent of any later edits or deletions in
     * the user's address book. Created from the live {@link Address}
     * entity via the {@link OrderAddressSnapshot#from} factory method.
     * The embedded mapping overrides column names with a {@code shipping_}
     * prefix to avoid conflicts with other address columns if present.</p>
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullName", column = @Column(name = "shipping_full_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "shipping_phone")),
            @AttributeOverride(name = "line1", column = @Column(name = "shipping_line1")),
            @AttributeOverride(name = "line2", column = @Column(name = "shipping_line2")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))
    })
    private OrderAddressSnapshot shippingAddress;

    /**
     * Current lifecycle status of the order, tracking its progression
     * from placement through fulfillment.
     *
     * <p>Stored as a string enum with a maximum length of 30 characters.
     * Transitions follow a defined state machine: PLACED, CONFIRMED,
     * SHIPPED, DELIVERED, or CANCELLED. Status changes trigger
     * corresponding notification emails and analytics events.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    /**
     * Payment method chosen by the customer at checkout (e.g. COD,
     * ONLINE).
     *
     * <p>Stored as a string enum with a maximum length of 20 characters.
     * This field determines whether a COD surcharge is applied and
     * influences the payment processing flow. The value must match one
     * of the predefined {@link PaymentMethod} enum constants.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * Delivery speed selected by the customer at checkout.
     *
     * <p>Stored as a string enum with a maximum length of 20 characters.
     * Defaults to {@link DeliveryType#NORMAL}. EXPRESS_1DAY incurs a
     * delivery surcharge of 7.50, while NORMAL delivery is free. The
     * delivery type influences both the shipping cost calculation and
     * the fulfillment SLA承诺.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 20)
    @Builder.Default
    private DeliveryType deliveryType = DeliveryType.NORMAL;

    /**
     * Current payment status tracking whether the order's payment has
     * been processed, confirmed, or refunded.
     *
     * <p>Stored as a string enum with a maximum length of 20 characters.
     * Transitions from PENDING to PAID (or FAILED/CANCELLED) and
     * optionally to REFUNDED. This status is updated by the payment
     * processing service and drives order fulfillment eligibility.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    /**
     * Sum of (unit price snapshot multiplied by quantity) for all
     * order items, representing the pre-tax, pre-discount total.
     *
     * <p>Calculated at checkout time and stored denormalized to ensure
     * historical accuracy. This value does not include delivery charges,
     * COD surcharges, tax, or discounts. Precision is 12 digits total
     * with 2 decimal places to accommodate large orders.</p>
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Delivery charge applied based on the selected delivery type.
     *
     * <p>Set to 7.50 for {@code EXPRESS_1DAY} delivery and 0 for
     * {@code NORMAL} delivery. Stored denormalized at checkout time
     * to preserve historical accuracy regardless of future changes to
     * the delivery pricing configuration.</p>
     */
    @Column(name = "delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge;

    /**
     * Cash-on-delivery (COD) surcharge applied when the customer
     * chooses COD as the payment method.
     *
     * <p>Set to 5.00 when the payment method is {@code COD} and 0
     * otherwise. Stored denormalized at checkout time to maintain
     * historical accuracy. This fee covers the additional handling
     * cost associated with cash collection at delivery.</p>
     */
    @Column(name = "cod_surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal codSurcharge;

    /**
     * Tax amount applied at checkout based on the applicable tax rules
     * for the order's shipping destination.
     *
     * <p>Calculated and stored denormalized at checkout time. The tax
     * rate and rules are determined by the service layer based on the
     * shipping address, product tax categories, and applicable
     * regulations. Precision is 10 digits total with 2 decimal places.</p>
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    /**
     * Discount amount applied at checkout, derived from the promo code
     * or promotional pricing.
     *
     * <p>Stored denormalized at checkout time. This value is subtracted
     * from the subtotal plus charges and tax to compute the grand total.
     * When no promo code is applied, this field is set to 0.00. The
     * discount is capped by the promo code's
     * {@code maxDiscountAmount} if applicable.</p>
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    /**
     * Final amount payable by the customer, calculated as
     * subtotal + deliveryCharge + codSurcharge + tax - discount.
     *
     * <p>Stored denormalized at checkout time to ensure historical
     * accuracy. This is the authoritative amount used for payment
     * processing, refund calculations, and financial reporting.
     * Precision is 12 digits total with 2 decimal places to handle
     * large order values.</p>
     */
    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    /**
     * Reference to the promo code applied at checkout, if any.
     *
     * <p>Snapshot reference preserved for historical orders. The promo
     * code entity may be deactivated or deleted after the order is
     * placed, but this FK allows the system to reconstruct the discount
     * context for customer support and analytics. Lazily loaded to
     * avoid unnecessary joins in order listing queries.</p>
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
     * <p>Preserves the code even if the original {@link PromoCode}
     * entity is later deleted or deactivated. Used by
     * {@code CheckoutResponse} and order history displays to show
     * which promo was applied without requiring a lazy-loaded join
     * to the {@code promo_codes} table.</p>
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
     * Timestamp when the order was placed by the customer.
     *
     * <p>Set to the current UTC time at the moment the checkout is
     * confirmed. This timestamp marks the beginning of the order
     * lifecycle and is used for order sorting, SLA tracking, and
     * analytics. Stored as an {@link Instant} for timezone
     * independence.</p>
     */
    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    /**
     * Line items that belong to this order, each representing a
     * purchased product with snapshotted details.
     *
     * <p>Managed via a one-to-many relationship with cascade all and
     * orphan removal. Adding or removing items should be done through
     * the {@link #addOrderItem(OrderItem)} helper method to maintain
     * bidirectional consistency. The collection is lazily loaded and
     * defaults to an empty list for newly created orders.</p>
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
     *
     * <p>Sets the parent {@link Order} reference on the item and adds
     * the item to this order's collection. Null-safe: no action if
     * {@code orderItem} is {@code null}.</p>
     *
     * @param orderItem order item to add
     */
    public void addOrderItem(OrderItem orderItem) {
        if (orderItem == null) return;
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
