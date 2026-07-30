package com.pkmprojects.shoppiq.config;

/**
 * Shared inventory-related constants used across admin and seller services.
 *
 * <p>This class centralizes magic numbers and threshold values that govern
 * inventory behavior across the application. By extracting these constants
 * into a single location, the codebase avoids scattered magic numbers and
 * provides a clear reference for business rules related to stock management.</p>
 *
 * <p>The constants are used by inventory services, admin dashboards, and
 * seller management screens to determine when to display low-stock
 * warnings, trigger reorder notifications, or apply special UI styling to
 * products that are running low on inventory.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class InventoryConstants {

    /**
     * Number of units at or below which a product is considered low on stock.
     *
     * <p>When an item's available quantity falls to this threshold or below,
     * the system flags it for low-stock attention. Admin dashboards display
     * visual indicators, and sellers receive notifications to restock.
     * This threshold is intentionally conservative to give sellers ample
     * time to replenish before running out completely.</p>
     */
    public static final int LOW_STOCK_THRESHOLD = 5;

    private InventoryConstants() {
    }
}
