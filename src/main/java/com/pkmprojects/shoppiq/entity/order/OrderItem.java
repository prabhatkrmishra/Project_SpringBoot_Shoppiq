package com.pkmprojects.shoppiq.entity.order;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a single line item inside an {@link Order}.
 *
 * <p>Product name and unit price are snapshotted from the catalog at the
 * moment of purchase so that historical orders remain accurate even if the
 * product is later edited or deleted. The line total (subtotal) is
 * pre-calculated and stored to avoid recalculation errors and ensure
 * consistency across order history, invoices, and refund calculations.</p>
 *
 * <p>An optional FK back to {@link ItemDetails} is retained for
 * retrospective audit and reporting purposes. The snapshot fields
 * (name, price) provide the authoritative data when the original
 * product is deleted, making the order self-contained for archival
 * and compliance requirements.</p>
 *
 * @author prabhatkrmishra
 * @see Order
 * @see ItemDetails
 * @since 1.0.0
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class OrderItem {

    /**
     * Unique identifier for this order line item.
     *
     * <p>Generated automatically by the database using the IDENTITY
     * strategy. This primary key uniquely identifies each line item
     * within the order and is used for item-level operations such as
     * quantity updates or line-item removal.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent order that this line item belongs to.
     *
     * <p>Required relationship. Each order item is associated with exactly
     * one order. The order reference is lazily loaded and maintained via
     * the {@code Order.addOrderItem()} helper to ensure bidirectional
     * consistency. Cascade behavior is managed by the owning
     * {@link Order} entity.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_order")
    )
    private Order order;

    /**
     * Reference to the purchased {@link ItemDetails} retained for
     * retrospective audit and reporting linkage.
     *
     * <p>Optional relationship. The name and price are stored as
     * snapshots so the order remains accurate even if the product is
     * later edited or deleted. This FK allows reporting queries to
     * join back to the original product for category analysis or
     * seller commission calculations without relying on snapshot
     * data.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "item_details_id",
            foreignKey = @ForeignKey(name = "fk_order_items_item_details")
    )
    private ItemDetails itemDetails;

    /**
     * Snapshot of the product name at the time of purchase.
     *
     * <p>Captured from {@link com.pkmprojects.shoppiq.entity.item.Item#getName()}
     * at checkout. This denormalized value ensures that order history,
     * invoices, and customer-facing displays show the product name as
     * it was when the order was placed, even if the product is later
     * renamed or removed from the catalog.</p>
     */
    @Column(name = "item_name_snapshot", nullable = false, length = 150)
    private String itemNameSnapshot;

    /**
     * Snapshot of the unit price at the time of purchase.
     *
     * <p>Captured from {@link ItemDetails#getPrice()} at checkout. This
     * denormalized value is the authoritative price for financial
     * calculations including subtotal, tax, discount, and refund
     * amounts. Precision is 10 digits total with 2 decimal places.</p>
     */
    @Column(name = "unit_price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceSnapshot;

    /**
     * Number of units of the product ordered in this line item.
     *
     * <p>Must be at least 1. The quantity is captured at checkout time
     * and represents the exact number of units purchased. This value
     * is multiplied by the unit price snapshot to compute the line
     * item subtotal.</p>
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Pre-calculated line total: unitPriceSnapshot multiplied by
     * quantity.
     *
     * <p>Stored denormalized at checkout time to ensure consistency
     * across order history, invoices, and financial reporting. This
     * value is summed across all order items to compute the order's
     * subtotal. Precision is 12 digits total with 2 decimal places
     * to accommodate large quantity orders.</p>
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
