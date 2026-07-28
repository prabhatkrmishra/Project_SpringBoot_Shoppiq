package com.pkmprojects.shoppiq.config;

/**
 * <strong>Spring Boot Concept:</strong> Shared inventory-related constants
 * used across admin and seller services.
 *
 * <p>The {@code LOW_STOCK_THRESHOLD} is used by inventory alerts and AI
 * assistant responses about product availability.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class InventoryConstants {

    private InventoryConstants() {
    }

    /**
     * Number of units at or below which a product is considered low on stock.
     */
    public static final int LOW_STOCK_THRESHOLD = 5;
}
