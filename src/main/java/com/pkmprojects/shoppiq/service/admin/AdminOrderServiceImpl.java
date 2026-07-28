package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.events.OrderStatusChangedEvent;
import com.pkmprojects.shoppiq.exception.general.order.OrderCannotBeCancelledException;
import com.pkmprojects.shoppiq.exception.general.order.OrderInvalidStatusTransitionException;
import com.pkmprojects.shoppiq.exception.general.order.OrderNotFoundException;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.service.order.OrderStatusTransitionValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link AdminOrderService}
 * containing business logic for admin order management.
 *
 * <p>Provides paginated order retrieval with optional status filtering, single
 * order lookup, and status transitions with workflow validation. Publishes
 * {@code OrderStatusChangedEvent} for async side effects. Used by
 * {@code AdminOrderController}.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Status updates and event publishing are atomic; reads use {@code readOnly = true}.</li>
 *   <li><strong>Constructor injection</strong> — final fields for immutability and testability.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see AdminOrderService
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderStatusTransitionValidator orderStatusTransitionValidator;

    public AdminOrderServiceImpl(OrderRepository orderRepository,
                                  ApplicationEventPublisher eventPublisher,
                                  OrderStatusTransitionValidator orderStatusTransitionValidator) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.orderStatusTransitionValidator = orderStatusTransitionValidator;
    }

    /**
     * Retrieves a paginated list of orders with optional status filtering, sorted newest-first.
     *
     * @param status optional order status filter
     * @param page   zero-based page index
     * @param size   page size
     * @return paginated order responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminOrderResponse> getAllOrders(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "placedAt"));

        var orderPage = (status != null)
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);

        return PageResponse.of(orderPage, AdminOrderResponse::fromEntity);
    }

    /**
     * Retrieves a single order by ID.
     *
     * @param orderId order ID
     * @return order response
     * @throws OrderNotFoundException if the order does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public AdminOrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '%d' was not found.".formatted(orderId)));
        return AdminOrderResponse.fromEntity(order);
    }

    /**
     * Updates the order status with workflow validation.
     *
     * <p>Enforces valid status transitions via {@code OrderStatus.canTransitionTo()}
     * and special cancellation rules. Publishes an {@code OrderStatusChangedEvent}
     * on success.</p>
     *
     * @param orderId   order ID
     * @param newStatus target order status
     * @return updated order response
     * @throws OrderNotFoundException                if the order does not exist
     * @throws OrderInvalidStatusTransitionException  if the transition is not allowed
     * @throws OrderCannotBeCancelledException        if cancellation is attempted from an invalid state
     */
    @Override
    public AdminOrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '%d' was not found.".formatted(orderId)));

        OrderStatus currentStatus = order.getStatus();

        // Validate transition using the shared state machine
        if (!orderStatusTransitionValidator.isValidTransition(currentStatus, newStatus)) {
            throw OrderInvalidStatusTransitionException.fromTo(currentStatus, newStatus);
        }

        // Special handling for cancellation — allow direct from PLACED or from CANCEL_REQUEST
        if (newStatus == OrderStatus.CANCELLED
                && currentStatus != OrderStatus.PLACED
                && currentStatus != OrderStatus.CANCEL_REQUEST) {
            throw OrderCannotBeCancelledException.forOrder(orderId, currentStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderStatusChangedEvent(order, currentStatus, newStatus));

        return AdminOrderResponse.fromEntity(order);
    }
}
