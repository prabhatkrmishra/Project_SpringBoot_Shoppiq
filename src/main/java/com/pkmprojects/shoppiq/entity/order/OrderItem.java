package com.pkmprojects.shoppiq.entity.order;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * <strong>Spring Boot Concept:</strong> Represents a single line item inside an {@link Order}.
 *
 * <p>
 * Product name and unit price are snapshotted from the catalog at the moment
 * of purchase so that historical orders remain accurate even if the product
 * is later edited or deleted.
 * </p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Snapshot pattern on order items</strong> — The
 *         {@code itemNameSnapshot} and {@code unitPriceSnapshot} fields
 *         capture the product state at purchase time. Even if the product
 *         is renamed, repriced, or deleted, the order record remains
 *         accurate for accounting and customer history.</li>
 *     <li><strong>{@code @ManyToOne} to {@link ItemDetails}</strong> — An
 *         optional FK reference back to the original product for
 *         retrospective audit/reporting. The FK is nullable because the
 *         product may be deleted later.</li>
 *     <li><strong>Optional FK + snapshot redundancy</strong> — The FK
 *         ({@code item_details_id}) is optional; the snapshot fields provide
 *         the authoritative data. This hybrid approach gives both
 *         referential integrity (when available) and historical accuracy.</li>
 *     <li><strong>Standalone entity</strong> — Unlike most entities,
 *         {@code OrderItem} does NOT extend {@code AuditableEntity}. It
 *         manages its own identity via {@code @Id} + generated value.</li>
 *     <li><strong>{@code subtotal} is stored, not computed</strong> — The
 *         line total is pre-calculated ({@code unitPriceSnapshot × quantity})
 *         and stored. This avoids recalculation errors when displaying
 *         historical orders.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
