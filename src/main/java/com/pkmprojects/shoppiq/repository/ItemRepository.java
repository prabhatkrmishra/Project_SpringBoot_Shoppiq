package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository responsible for {@link Item} persistence.
 *
 * <p>
 * Provides CRUD operations together with catalog-specific lookup
 * methods required by the business layer.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface ItemRepository
        extends JpaRepository<Item, Long> {

    /**
     * Finds an item using its SKU.
     *
     * @param sku stock keeping unit
     * @return matching item if present
     */
    Optional<Item> findByItemDetailsSku(String sku);

    /**
     * Determines whether a SKU already exists.
     *
     * @param sku stock keeping unit
     * @return true if found
     */
    boolean existsByItemDetailsSku(String sku);

    /**
     * Determines whether another item already owns the supplied SKU.
     *
     * <p>
     * Used during update operations.
     * </p>
     *
     * @param sku SKU
     * @param id  item identifier to ignore
     * @return true if duplicate exists
     */
    boolean existsByItemDetailsSkuAndIdNot(
            String sku,
            Long id
    );

    /**
     * Retrieves all items ordered alphabetically.
     *
     * @return ordered item list
     */
    List<Item> findAllByOrderByNameAsc();

    /**
     * Retrieves every SKU that already exists in the database.
     *
     * <p>
     * Used during bulk creation to validate all SKUs using a single
     * database query instead of executing one query per item.
     * </p>
     *
     * @param skus SKUs received in the request
     * @return existing SKUs
     */
    @Query("""
            select i.itemDetails.sku
            from Item i
            where i.itemDetails.sku in :skus
            """)
    Set<String> findExistingSkus(@Param("skus") Set<String> skus);
}