package com.pkmprojects.shoppiq.repository.cart;

import com.pkmprojects.shoppiq.entity.cart.Cart;
import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for the {@link Cart} aggregate.
 *
 * <p>Provides methods to query carts by user with eager fetching of cart items and associations
 * for checkout processing. The repository supports both simple cart lookups and optimized
 * queries with JOIN FETCH to avoid N+1 queries during cart display and checkout.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Returns the cart owned by the given user.
     *
     * @param user the authenticated user
     * @return the user's cart, if it exists
     */
    Optional<Cart> findByUser(User user);

    /**
     * Returns the cart owned by the given user with items, item details,
     * and related entities eagerly fetched via JOIN FETCH to avoid N+1
     * queries during checkout.
     *
     * <p>Uses an explicit {@code @Query} because the method name
     * {@code findByUserWithItems} is misinterpreted by Spring Data's
     * derived query parser as {@code findByUser} + {@code WithItems}
     * (a property path on {@link User}).</p>
     *
     * @param user the authenticated user
     * @return the user's cart with associations loaded, if it exists
     */
    @Query("""
            SELECT c FROM Cart c
            LEFT JOIN FETCH c.items i
            LEFT JOIN FETCH i.itemDetails id
            LEFT JOIN FETCH id.item
            LEFT JOIN FETCH id.category
            WHERE c.user = :user""")
    Optional<Cart> findByUserWithItems(@Param("user") User user);
}
