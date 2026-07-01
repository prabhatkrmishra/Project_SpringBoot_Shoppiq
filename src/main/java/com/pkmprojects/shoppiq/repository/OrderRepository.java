package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Order} persistence.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Returns all orders placed by the given user, newest first.
     *
     * @param user the customer
     * @return list of orders
     */
    List<Order> findAllByUserOrderByPlacedAtDesc(User user);

    /**
     * Returns a paginated view of all orders.
     *
     * @param pageable pagination params
     * @return page of orders
     */
    Page<Order> findAll(Pageable pageable);

    /**
     * Looks up a single order by id.
     *
     * @param id order id
     * @return optional order
     */
    Optional<Order> findById(Long id);
}
