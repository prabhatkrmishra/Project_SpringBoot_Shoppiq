package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminOrderResponse;
import com.pkmprojects.shoppiq.enums.OrderStatus;

import java.util.List;

/**
 * Business contract for admin order management.
 *
 * <p>
 * Defines the operations for managing customer orders,
 * including retrieval and status transitions.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Retrieve all orders with pagination.</li>
 *     <li>Retrieve a single order by ID.</li>
 *     <li>Update order status with validation.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Works exclusively with DTOs.</li>
 *     <li>Enforces valid status transition flow.</li>
 *     <li>Implemented by {@code AdminOrderServiceImpl}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * PLACED → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED → RETURNED
     * PLACED → CANCELLED (at any point before DELIVERED)
     * </pre>
     *
     * @param orderId   order identifier
     * @param newStatus new status to transition to
     * @return updated order response
     */
    AdminOrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);

    /**
     * Page response wrapper.
     *
     * @param <T> content type
     */
    record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }
}