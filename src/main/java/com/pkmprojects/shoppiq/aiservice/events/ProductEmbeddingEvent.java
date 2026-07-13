package com.pkmprojects.shoppiq.aiservice.events;

import java.math.BigDecimal;

/**
 * Carries the minimal product information needed to (re)build a vector-store
 * embedding. Published by {@link ItemEmbeddingEntityListener} on JPA lifecycle
 * events and consumed by {@code ProductCatalogIngester} after transaction commit.
 *
 * <p>
 * Using a flat DTO (rather than the {@code Item} entity) avoids lazy-loading
 * the {@code itemDetails}/{@code category} associations once the transaction has
 * closed in the after-commit handler.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public class ProductEmbeddingEvent {

    private final Long itemId;
    private final String name;
    private final String description;
    private final String slug;
    private final BigDecimal price;
    private final String categorySlug;
    private final String categoryName;
    private final String brand;
    private final int stockQuantity;
    private final boolean deleted;

    public ProductEmbeddingEvent(Long itemId, String name, String description, String slug,
                                 BigDecimal price, String categorySlug, String categoryName,
                                 String brand, int stockQuantity, boolean deleted) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.price = price;
        this.categorySlug = categorySlug;
        this.categoryName = categoryName;
        this.brand = brand;
        this.stockQuantity = stockQuantity;
        this.deleted = deleted;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSlug() {
        return slug;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getBrand() {
        return brand;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
