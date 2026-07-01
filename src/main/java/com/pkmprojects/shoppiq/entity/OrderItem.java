package com.pkmprojects.shoppiq.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a single line item inside an {@link Order}.
 *
 * <p>
 * Product name and unit price are snapshotted from the catalog at the moment
 * of purchase so that historical orders remain accurate even if the product
 * is later edited or deleted.
 * </p>
 *
 * @author PrabhatKrMishra
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
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent order.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_order")
    )
    private Order order;

    /**
     * Reference to the purchased {@link ItemDetails}.
     *
     * <p>Kept for potential audit/reporting linkage.
     * The name and price are stored as snapshots so the order remains
     * accurate even if the product changes later.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "item_details_id",
            foreignKey = @ForeignKey(name = "fk_order_items_item_details")
    )
    private ItemDetails itemDetails;

    /**
     * Snapshot of the product name at the time of purchase.
     */
    @Column(name = "item_name_snapshot", nullable = false, length = 150)
    private String itemNameSnapshot;

    /**
     * Snapshot of the unit price at the time of purchase.
     */
    @Column(name = "unit_price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceSnapshot;

    /**
     * Quantity ordered.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Line total: unitPriceSnapshot × quantity.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
