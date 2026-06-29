package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Cart;
import com.pkmprojects.shoppiq.entity.User;
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
     * Returns {@code true} if the given user already has a cart.
     *
     * @param user the authenticated user
     * @return {@code true} when a cart exists
     */
    boolean existsByUser(User user);
}
