package com.pkmprojects.shoppiq.repository.cart;

import com.pkmprojects.shoppiq.entity.cart.Cart;
import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link Cart} persistence operations.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Simple derived query</strong> — {@code findByUser} generates
 *       {@code SELECT * FROM carts WHERE user_id = ?}.</li>
 *   <li><strong>Custom JPQL with JOIN FETCH</strong> — {@code findByUserWithItems} shows
 *       how to eagerly load associations (items, itemDetails, category, item) in a single
 *       query to avoid N+1 performance problems during checkout.</li>
 *   <li><strong>When to fall back to {@code @Query}</strong> — The Javadoc on
 *       {@link #findByUserWithItems} explains that Spring Data's parser misinterprets
 *       compound method names like {@code findByUserWithItems}, making {@code @Query}
 *       the correct approach for complex fetch strategies.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findByUser(User)
 *       → SELECT * FROM carts WHERE user_id = ?
 *   findByUserWithItems(@Query)
 *       → SELECT c FROM carts c LEFT JOIN FETCH c.items ...
 * </pre>
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
