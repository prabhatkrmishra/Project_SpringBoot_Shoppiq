package com.pkmprojects.shoppiq.repository.seller;

import com.pkmprojects.shoppiq.entity.seller.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Persistence operations for the {@link Store} aggregate.
 *
 * <p>Provides methods to query stores by slug, seller ID, and existence checks for store management
 * and URL resolution. The repository supports slug-based lookups for public store pages and
 * seller-based lookups for store management workflows.</p>
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

    /**
     * Checks whether a store exists with the given slug (lightweight count query).
     *
     * @param slug the store slug
     * @return true if a store with that slug exists
     */
    boolean existsBySlug(String slug);
}
