package com.pkmprojects.shoppiq.repository.seller;

import com.pkmprojects.shoppiq.entity.seller.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link Store} entities.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived query by slug</strong> — {@code findBySlug} generates
 *       {@code SELECT * FROM stores WHERE slug = ?}, used for public store pages.</li>
 *   <li><strong>Derived query by association ID</strong> — {@code findBySellerId} navigates
 *       the {@code seller} foreign key, generating {@code SELECT * FROM stores WHERE seller_id = ?}.</li>
 *   <li><strong>Optional return type</strong> — Both methods return
 *       {@link java.util.Optional}, the standard pattern for finders that may return
 *       zero or one result.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findBySlug(String)
 *       → SELECT * FROM stores WHERE slug = ?
 *   findBySellerId(Long)
 *       → SELECT * FROM stores WHERE seller_id = ?
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * Finds a store by its URL slug.
     *
     * @param slug the store slug
     * @return matching store if present
     */
    Optional<Store> findBySlug(String slug);

    /**
     * Finds a store by the associated seller's ID.
     *
     * @param sellerId the seller ID
     * @return matching store if present
     */
    Optional<Store> findBySellerId(Long sellerId);
}
