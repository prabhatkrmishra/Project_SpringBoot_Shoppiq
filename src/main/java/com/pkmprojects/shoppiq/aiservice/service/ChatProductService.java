package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.item.Item;

import java.util.List;
import java.util.Optional;

/**
 * Read-only product query facade for the AI chat assistant.
 *
 * <p>This interface provides a narrow, AI-specific query surface for
 * product data access. It decouples the AI tool methods from the
 * underlying item repository, allowing the AI layer to query products
 * without depending on the full item service API. This separation
 * also enables read-only transactional boundaries for AI queries,
 * preventing accidental data modifications from tool invocations.</p>
 *
 * <p>The interface is intentionally minimal, exposing only the query
 * operations needed by the AI tools: slug-based lookup and name-based
 * search.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface ChatProductService {

    /**
     * Finds a product by its URL-friendly slug identifier.
     *
     * <p>Used by the product detail tool to retrieve a specific product
     * when the user mentions it by name or the AI model identifies it
     * from conversation context. Returns an empty Optional if no product
     * matches the given slug.</p>
     *
     * @param slug URL-friendly identifier (e.g., "wireless-headphones")
     * @return matching product, or empty if not found
     */
    Optional<Item> findBySlug(String slug);

    /**
     * Finds products whose name contains the given text (case-insensitive).
     *
     * <p>Used as a fallback when the slug-based lookup fails, providing
     * approximate matching for product name queries. Results are limited
     * to the specified maximum count.</p>
     *
     * @param name  text to search for (case-insensitive substring match)
     * @param limit maximum number of results to return
     * @return matching products
     */
    List<Item> findByNameContaining(String name, int limit);
}
