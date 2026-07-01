package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link OrderItem} persistence.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Returns all line items for the given order.
     *
     * @param order the parent order
     * @return list of order items
     */
    List<OrderItem> findAllByOrder(Order order);
}
