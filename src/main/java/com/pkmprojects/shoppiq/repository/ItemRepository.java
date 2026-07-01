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
     * Retrieves all items with their item details eagerly fetched.
     *
     * @return list of items with item details
     */
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category")
    List<Item> findAllWithItemDetails();

    /**
     * Returns the subset of the given SKUs that already exist in the database.
     *
     * @param skus set of SKUs to check
     * @return subset of SKUs that already exist
     */
    @Query("SELECT id.sku FROM ItemDetails id WHERE id.sku IN :skus")
    Set<String> findExistingSkus(@Param("skus") Set<String> skus);
}