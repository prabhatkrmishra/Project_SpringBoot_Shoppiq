package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.events.OrderStatusChangedEvent;
import com.pkmprojects.shoppiq.exception.general.order.OrderInvalidStatusTransitionException;
import com.pkmprojects.shoppiq.exception.general.order.OrderNotFullyOwnedException;
import com.pkmprojects.shoppiq.exception.general.order.OrderNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerSuspendedException;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.service.order.OrderStatusTransitionValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <strong>Spring Boot Concept:</strong> Default implementation of {@link SellerOrderService}.
 *
 * <p>
 * Provides order management for sellers. Sellers can view orders
 * containing their products and update status only when all items
 * in the order belong to them.
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Service
@Transactional
public class SellerOrderServiceImpl implements SellerOrderService {

    private final SellerLookupService sellerLookupService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderStatusTransitionValidator orderStatusTransitionValidator;

    public SellerOrderServiceImpl(SellerLookupService sellerLookupService,
                                  OrderRepository orderRepository,
                                  ApplicationEventPublisher eventPublisher,
                                  OrderStatusTransitionValidator orderStatusTransitionValidator) {
        this.sellerLookupService = sellerLookupService;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.orderStatusTransitionValidator = orderStatusTransitionValidator;
    }

    /**
     * Retrieves a paginated list of orders containing the seller's products.
     *
     * @param user authenticated user
     * @param page zero-based page index
     * @param size page size
     * @return paginated seller order responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerOrderResponse> getOrders(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        Pageable pageable = PageRequest.of(page, size);
        var orderPage = orderRepository.findDistinctBySellerId(seller.getId(), pageable);
        return PageResponse.of(orderPage, order -> SellerOrderResponse.from(order, seller.getId()));
    }

    /**
     * Retrieves a single order by ID, ensuring it contains the seller's products.
     *
     * @param user    authenticated user
     * @param orderId order ID
     * @return seller order response
     * @throws OrderNotFoundException if the order does not exist or contains no seller items
     */
    @Override
    @Transactional(readOnly = true)
    public SellerOrderResponse getOrder(User user, Long orderId) {
        Seller seller = findActiveSeller(user);
        Order order = findOrderOrThrow(orderId);

        if (orderRepository.countSellerItemsInOrder(orderId, seller.getId()) == 0) {
            throw OrderNotFoundException.id(orderId);
        }

        return SellerOrderResponse.from(order, seller.getId());
    }

    /**
     * Updates the order status with seller ownership validation.
     *
     * <p>Only allows status changes when all items in the order belong to the
     * seller. Publishes an {@code OrderStatusChangedEvent} on success.</p>
     *
     * @param user      authenticated user
     * @param orderId   order ID
     * @param newStatus target order status
     * @return updated seller order response
     * @throws OrderNotFoundException                if the order does not exist
     * @throws OrderNotFullyOwnedException            if not all items belong to the seller
     * @throws OrderInvalidStatusTransitionException  if the status transition is not allowed
     */
    @Override
    public SellerOrderResponse updateOrderStatus(User user, Long orderId, OrderStatus newStatus) {
        Seller seller = findActiveSeller(user);
        Order order = findOrderOrThrow(orderId);

        long sellerItemCount = orderRepository.countSellerItemsInOrder(orderId, seller.getId());
        if (sellerItemCount == 0) {
            throw OrderNotFoundException.id(orderId);
        }

        long totalItemCount = orderRepository.countTotalItemsInOrder(orderId);
        if (sellerItemCount != totalItemCount) {
            throw OrderNotFullyOwnedException.forOrder(orderId);
        }

        OrderStatus currentStatus = order.getStatus();
        if (!orderStatusTransitionValidator.isValidTransition(currentStatus, newStatus)) {
            throw OrderInvalidStatusTransitionException.fromTo(currentStatus, newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderStatusChangedEvent(order, currentStatus, newStatus));

        return SellerOrderResponse.from(order, seller.getId());
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> OrderNotFoundException.id(orderId));
    }

    private Seller findActiveSeller(User user) {
        Seller seller = sellerLookupService.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));

        if (seller.getSellerStatus() == SellerStatus.SUSPENDED) {
            throw SellerSuspendedException.forAction(seller.getId(), "manage orders");
        }

        if (seller.getSellerStatus() != SellerStatus.ACTIVE
                || seller.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw SellerNotVerifiedException.forAction(seller.getId(), "manage orders");
        }

        return seller;
    }
}
