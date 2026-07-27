package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.user.User;

import java.util.List;
import java.util.Optional;

/**
 * Read-only order query facade for the AI chat assistant.
 *
 * <p>Decouples {@code ShoppiqTools} from {@code OrderRepository},
 * providing a narrow, AI-specific query surface that returns
 * raw entities for text formatting in tool responses.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
public interface ChatOrderService {

    /**
     * Finds an order by ID.
     *
     * @param orderId order identifier
     * @return matching order, or empty if not found
     */
    Optional<Order> findById(Long orderId);

    /**
     * Returns all orders for a user, most recent first.
     *
     * @param user order owner
     * @return ordered list of orders
     */
    List<Order> findByUserNewestFirst(User user);
}
