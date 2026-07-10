package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;

/**
 * Business contract for seller order management.
 *
 * <p>
 * Allows sellers to view orders containing their products and update
 * order status when all items in the order belong to them.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>List orders containing the seller's products.</li>
 *     <li>View a specific order filtered to the seller's line items.</li>
 *     <li>Update order status (only when all items belong to the seller).</li>
 *     <li>Enforce seller-level preconditions (ACTIVE, not SUSPENDED).</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface SellerOrderService {

    /**
     * Retrieves all orders containing the authenticated seller's products, paginated.
     *
     * @param user the authenticated user
     * @param page page number (0-based)
     * @param size page size
     * @return paginated orders with seller's items
     */
    PageResponse<SellerOrderResponse> getOrders(User user, int page, int size);

    /**
     * Retrieves a specific order filtered to the seller's line items.
     *
     * @param user    the authenticated user
     * @param orderId the order identifier
     * @return order details with seller's items only
     */
    SellerOrderResponse getOrder(User user, Long orderId);

    /**
     * Updates the status of an order. The seller may only update the
     * status when all items in the order belong to them.
     *
     * @param user      the authenticated user
     * @param orderId   the order identifier
     * @param newStatus the new order status
     * @return updated order details with seller's items
     */
    SellerOrderResponse updateOrderStatus(User user, Long orderId, OrderStatus newStatus);
}
