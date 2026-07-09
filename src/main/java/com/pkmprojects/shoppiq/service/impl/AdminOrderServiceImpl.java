package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.OrderEmailService;
import com.pkmprojects.shoppiq.service.admin.AdminOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        Page<Order> orderPage = (status != null)
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);

        List<AdminOrderResponse> content = orderPage.getContent().stream()
                .map(AdminOrderResponse::fromEntity)
                .toList();

        return new PageResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast()
        );
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

        // Special handling for cancellation
        if (newStatus == OrderStatus.CANCELLED && currentStatus != OrderStatus.PLACED) {
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
     * <p>Valid flow: PLACED → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
     * CANCELLED can only be applied from PLACED status.</p>
     */
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return false; // No change
        }

        if (to == OrderStatus.CANCELLED) {
            return from == OrderStatus.PLACED;
        }

        // Forward flow
        if (from == OrderStatus.PLACED) {
            return to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
        }
        if (from == OrderStatus.CONFIRMED) {
            return to == OrderStatus.SHIPPED;
        }
        if (from == OrderStatus.SHIPPED) {
            return to == OrderStatus.OUT_FOR_DELIVERY;
        }
        if (from == OrderStatus.OUT_FOR_DELIVERY) {
            return to == OrderStatus.DELIVERED;
        }
        if (from == OrderStatus.DELIVERED) {
            return to == OrderStatus.RETURNED;
        }

        // RETURNED is terminal
        return false;
    }
}