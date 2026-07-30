package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminOrderResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.enums.OrderStatus;

/**
 * Business contract for admin order management and status transitions.
 *
 * <p>Defines operations for retrieving orders with filtering and updating
 * order status with workflow validation.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface AdminOrderService {

    /**
     * Retrieves all orders with optional filtering.
     *
     * @param status optional status filter
     * @param page   page number (0-based)
     * @param size   page size
     * @return paginated order responses
     */
    PageResponse<AdminOrderResponse> getAllOrders(OrderStatus status, int page, int size);

    /**
     * Retrieves a single order by ID.
     *
     * @param orderId order identifier
     * @return order response
     */
    AdminOrderResponse getOrderById(Long orderId);

    /**
     * Updates order status with workflow validation.
     *
     * <p>Valid transitions:</p>
     * <pre>
     * PLACED → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
     * PLACED → CANCEL_REQUEST → CANCELLED
     * PLACED → CANCELLED (direct)
     * DELIVERED → RETURN_REQUEST → RETURN_PICKUP → RETURNED
     * DELIVERED → REFUND_REQUEST → RETURN_PICKUP → REFUNDED
     * DELIVERED → REPLACE_REQUEST → REPLACE_PICKUP → REPLACED
     * </pre>
     *
     * @param orderId   order identifier
     * @param newStatus new status to transition to
     * @return updated order response
     */
    AdminOrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);
}
