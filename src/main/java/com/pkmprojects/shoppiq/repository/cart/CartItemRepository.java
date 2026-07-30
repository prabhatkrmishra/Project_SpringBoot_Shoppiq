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
 * Persistence operations for the {@link CartItem} aggregate.
 *
 * <p>Provides methods to query cart items by cart with eager fetching of product associations
 * for cart display and checkout processing. The repository supports duplicate detection
 * for cart item creation and optimized queries to avoid N+1 issues during cart rendering.</p>
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
