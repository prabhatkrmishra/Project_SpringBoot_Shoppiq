package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.user.User;

import java.util.List;
import java.util.Optional;

/**
 * Read-only order query facade for the AI chat assistant.
 *
 * <p>This interface provides a narrow, AI-specific query surface for
 * order data access. It decouples the AI tool methods from the
 * order repository, allowing the AI layer to query orders without
 * depending on the full order service API. This separation ensures
 * that AI tool invocations operate within read-only transactional
 * boundaries.</p>
 *
 * <p>The interface exposes only the query operations needed by the
 * order status tool: single-order lookup and user-order listing.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface ChatOrderService {

    /**
     * Finds an order by its unique identifier.
     *
     * <p>Used by the order status tool to retrieve a specific order when the
     * user provides an order number. Returns an empty Optional if no order
     * matches the given ID.</p>
     *
     * @param orderId order identifier
     * @return matching order, or empty if not found
     */
    Optional<Order> findById(Long orderId);

    /**
     * Returns all orders for a user, most recent first.
     *
     * <p>Used by the order status tool to retrieve the user's recent order
     * history. Results are ordered by placement date descending and limited
     * to the most recent orders.</p>
     *
     * @param user order owner
     * @return ordered list of orders
     */
    List<Order> findByUserNewestFirst(User user);
}
