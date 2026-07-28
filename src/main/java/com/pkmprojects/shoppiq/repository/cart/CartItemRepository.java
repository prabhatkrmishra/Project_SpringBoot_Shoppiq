package com.pkmprojects.shoppiq.repository.cart;

import com.pkmprojects.shoppiq.entity.cart.Cart;
import com.pkmprojects.shoppiq.entity.cart.CartItem;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link CartItem} persistence operations.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Cross-entity derived queries</strong> — {@code findByCartAndItemDetails}
 *       navigates across {@link com.pkmprojects.shoppiq.entity.cart.Cart} and
 *       {@link com.pkmprojects.shoppiq.entity.item.ItemDetails} associations, generating
 *       {@code SELECT * FROM cart_items WHERE cart_id = ? AND item_details_id = ?}.</li>
 *   <li><strong>Composition via method naming</strong> — Combining entity parameters
 *       with {@code And} to express multi-field lookups without writing JPQL.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findByCartAndItemDetails(Cart, ItemDetails)
 *       → SELECT * FROM cart_items WHERE cart_id = ? AND item_details_id = ?
 *   findAllByCart(Cart)
 *       → SELECT * FROM cart_items WHERE cart_id = ?
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Finds a specific cart item by cart and product.
     *
     * <p>Used to detect duplicates before creating a new line item.</p>
     *
     * @param cart        the cart to search in
     * @param itemDetails the product to look for
     * @return the existing cart item, if found
     */
    Optional<CartItem> findByCartAndItemDetails(Cart cart, ItemDetails itemDetails);

    /**
     * Returns all items belonging to the given cart, with item details
     * and the associated product eagerly fetched (BUG-006).
     *
     * @param cart the cart whose items are to be retrieved
     * @return list of cart items
     */
    @EntityGraph(attributePaths = {"itemDetails", "itemDetails.item"})
    List<CartItem> findAllByCart(Cart cart);
}
