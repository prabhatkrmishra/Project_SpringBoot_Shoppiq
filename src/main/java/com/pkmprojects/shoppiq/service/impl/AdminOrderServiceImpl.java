package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.OrderEmailService;
import com.pkmprojects.shoppiq.service.admin.AdminOrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link AdminOrderService}.
 *
 * <p>
 * Provides order management operations for administrators
 * including retrieval and status transitions with workflow
 * validation.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Retrieve paginated orders with optional status filter.</li>
 *     <li>Retrieve single order by ID.</li>
 *     <li>Update order status with valid transition enforcement.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>Read operations use read-only transactions.</li>
 *     <li>Status transitions enforce business workflow rules.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderEmailService orderEmailService;

    public AdminOrderServiceImpl(OrderRepository orderRepository, OrderEmailService orderEmailService) {
        this.orderRepository = orderRepository;
        this.orderEmailService = orderEmailService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminOrderResponse> getAllOrders(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "placedAt"));

        var orderPage = (status != null)
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);

        return PageResponse.of(orderPage, AdminOrderResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '%d' was not found.".formatted(orderId)));
        return AdminOrderResponse.fromEntity(order);
    }

    @Override
    public AdminOrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '%d' was not found.".formatted(orderId)));

        OrderStatus currentStatus = order.getStatus();

        // Validate transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new OrderInvalidStatusTransitionException(currentStatus, newStatus);
        }

        // Special handling for cancellation — allow direct from PLACED or from CANCEL_REQUEST
        if (newStatus == OrderStatus.CANCELLED
                && currentStatus != OrderStatus.PLACED
                && currentStatus != OrderStatus.CANCEL_REQUEST) {
            throw new OrderCannotBeCancelledException(orderId, currentStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        try {
            orderEmailService.sendOrderStatusEmail(order, newStatus);
        } catch (Exception e) {
            // Don't fail the status update if email fails
        }

        return AdminOrderResponse.fromEntity(order);
    }

    /**
     * Validates if a status transition is allowed.
     *
     * <p>Valid flows:</p>
     * <pre>
     * PLACED → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
     * PLACED → CANCEL_REQUEST → CANCELLED
     * PLACED → CANCELLED (direct)
     * DELIVERED → RETURN_REQUEST → RETURN_PICKUP → RETURNED
     * DELIVERED → REFUND_REQUEST → RETURN_PICKUP → REFUNDED
     * DELIVERED → REPLACE_REQUEST → REPLACE_PICKUP → REPLACED
     * </pre>
     */
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return false;
        }

        // PLACED can go to CONFIRMED, CANCEL_REQUEST, or CANCELLED
        if (from == OrderStatus.PLACED) {
            return to == OrderStatus.CONFIRMED
                    || to == OrderStatus.CANCEL_REQUEST
                    || to == OrderStatus.CANCELLED;
        }

        // CANCEL_REQUEST can only go to CANCELLED
        if (from == OrderStatus.CANCEL_REQUEST) {
            return to == OrderStatus.CANCELLED;
        }

        // Normal forward flow
        if (from == OrderStatus.CONFIRMED) {
            return to == OrderStatus.SHIPPED;
        }
        if (from == OrderStatus.SHIPPED) {
            return to == OrderStatus.OUT_FOR_DELIVERY;
        }
        if (from == OrderStatus.OUT_FOR_DELIVERY) {
            return to == OrderStatus.DELIVERED;
        }

        // DELIVERED can go to RETURN_REQUEST, REFUND_REQUEST, or REPLACE_REQUEST
        if (from == OrderStatus.DELIVERED) {
            return to == OrderStatus.RETURN_REQUEST
                    || to == OrderStatus.REFUND_REQUEST
                    || to == OrderStatus.REPLACE_REQUEST;
        }

        // RETURN_REQUEST can go to RETURN_PICKUP
        if (from == OrderStatus.RETURN_REQUEST) {
            return to == OrderStatus.RETURN_PICKUP;
        }

        // REFUND_REQUEST can go to RETURN_PICKUP
        if (from == OrderStatus.REFUND_REQUEST) {
            return to == OrderStatus.RETURN_PICKUP;
        }

        // REPLACE_REQUEST can go to REPLACE_PICKUP
        if (from == OrderStatus.REPLACE_REQUEST) {
            return to == OrderStatus.REPLACE_PICKUP;
        }

        // RETURN_PICKUP can go to RETURNED or REFUNDED
        if (from == OrderStatus.RETURN_PICKUP) {
            return to == OrderStatus.RETURNED || to == OrderStatus.REFUNDED;
        }

        // REPLACE_PICKUP can go to REPLACED
        if (from == OrderStatus.REPLACE_PICKUP) {
            return to == OrderStatus.REPLACED;
        }

        // CANCELLED, RETURNED, REFUNDED, REPLACED are terminal
        return false;
    }
}