package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Cart;
import com.pkmprojects.shoppiq.entity.CartItem;
import com.pkmprojects.shoppiq.entity.ItemDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link CartItem} persistence operations.
 *
 * @author PrabhatKrMishra
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
     * Returns all items belonging to the given cart.
     *
     * @param cart the cart whose items are to be retrieved
     * @return list of cart items
     */
    List<CartItem> findAllByCart(Cart cart);
}
