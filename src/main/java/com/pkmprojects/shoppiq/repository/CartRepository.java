package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Cart;
import com.pkmprojects.shoppiq.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link Cart} persistence operations.
 *
 * @author PrabhatKrMishra
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
     * and items eagerly fetched to avoid N+1 queries during checkout.
     *
     * @param user the authenticated user
     * @return the user's cart with associations loaded, if it exists
     */
    @EntityGraph(attributePaths = {"items", "items.itemDetails", "items.itemDetails.item", "items.itemDetails.category"})
    Optional<Cart> findByUserWithItems(User user);
}
