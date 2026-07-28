package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminOrderResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.enums.OrderStatus;

/**
 * <strong>Spring Boot Concept:</strong> Business contract for admin order management.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * This interface defines the <strong>Service layer</strong> contract for admin order operations.
 * The architecture follows: {@code AdminOrderController → AdminOrderService → OrderRepository}.
 * Controllers depend on this abstraction, not on the concrete implementation.
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *     <li>Retrieve all orders with optional status filtering and pagination.</li>
 *     <li>Retrieve a single order by ID.</li>
 *     <li>Update order status with <strong>workflow validation</strong> — enforces valid state
 *         transitions (e.g., PLACED → CONFIRMED, not DELIVERED → PLACED).</li>
 *     <li>Publish domain events ({@code OrderStatusChangedEvent}) when status changes.</li>
 * </ul>
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
