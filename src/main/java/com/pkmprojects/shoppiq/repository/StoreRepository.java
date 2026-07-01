package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link Store} entities.
 *
 * @author PrabhatKrMishra
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
