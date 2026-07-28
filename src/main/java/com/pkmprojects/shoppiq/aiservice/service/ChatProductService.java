package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.item.Item;

import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Read-only product query facade for the AI chat assistant.
 *
 * <p>Decouples {@code ShoppiqTools} from {@code ItemRepository},
 * providing a narrow, AI-specific query surface that returns
 * raw entities for text formatting in tool responses.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
public interface ChatProductService {

    /**
     * Finds a product by its URL slug.
     *
     * @param slug URL-friendly identifier
     * @return matching product, or empty if not found
     */
    Optional<Item> findBySlug(String slug);

    /**
     * Finds products whose name contains the given text (case-insensitive).
     *
     * @param name  text to search for
     * @param limit maximum results
     * @return matching products
     */
    List<Item> findByNameContaining(String name, int limit);
}
