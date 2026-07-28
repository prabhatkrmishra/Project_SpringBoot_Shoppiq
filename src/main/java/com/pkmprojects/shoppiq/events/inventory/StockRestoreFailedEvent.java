package com.pkmprojects.shoppiq.events.inventory;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Event published when stock restoration fails due to optimistic locking conflicts.
 *
 * <p>This event allows other systems to react to stock restoration failures
 * and implement retry mechanisms or manual intervention.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public class StockRestoreFailedEvent extends ApplicationEvent {

    private final String sku;
    private final Long orderId;
    private final Integer quantity;
    private final String errorMessage;
    private final Instant timestamp;

    /**
     * Create a new StockRestoreFailedEvent.
     *
     * @param source the object on which the event initially occurred (typically the InventoryService)
     * @param sku the SKU of the item that failed to restore
     * @param orderId the order ID associated with the failed restoration
     * @param quantity the quantity that failed to restore
     * @param errorMessage the error message from the optimistic locking failure
     */
    public StockRestoreFailedEvent(Object source, String sku, Long orderId, Integer quantity, String errorMessage) {
        super(source);
        this.sku = sku;
        this.orderId = orderId;
        this.quantity = quantity;
        this.errorMessage = errorMessage;
        this.timestamp = Instant.now();
    }

    /**
     * Get the SKU of the item that failed to restore.
     *
     * @return the SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Get the order ID associated with the failed restoration.
     *
     * @return the order ID
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * Get the quantity that failed to restore.
     *
     * @return the quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Get the error message from the optimistic locking failure.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the timestamp when the event was created.
     *
     * @return the timestamp
     */
    public Instant getCreatedAt() {
        return timestamp;
    }
}
