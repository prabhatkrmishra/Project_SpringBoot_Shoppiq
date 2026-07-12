package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.Seller;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.OrderInvalidStatusTransitionException;
import com.pkmprojects.shoppiq.exception.OrderNotFullyOwnedException;
import com.pkmprojects.shoppiq.exception.OrderNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.SellerSuspendedException;
import com.pkmprojects.shoppiq.repository.OrderRepository;
import com.pkmprojects.shoppiq.repository.SellerRepository;
import com.pkmprojects.shoppiq.service.OrderEmailService;
import com.pkmprojects.shoppiq.service.seller.SellerOrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link SellerOrderService}.
 *
 * <p>
 * Provides order management for sellers. Sellers can view orders
 * containing their products and update status only when all items
 * in the order belong to them.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class SellerOrderServiceImpl implements SellerOrderService {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final OrderEmailService orderEmailService;

    public SellerOrderServiceImpl(SellerRepository sellerRepository,
                                  OrderRepository orderRepository,
                                  OrderEmailService orderEmailService) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
        this.orderEmailService = orderEmailService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerOrderResponse> getOrders(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        Pageable pageable = PageRequest.of(page, size);
        var orderPage = orderRepository.findDistinctBySellerId(seller.getId(), pageable);
        return PageResponse.of(orderPage, order -> SellerOrderResponse.from(order, seller.getId()));
    }

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
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new OrderInvalidStatusTransitionException(currentStatus, newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        try {
            orderEmailService.sendOrderStatusEmail(order, newStatus);
        } catch (Exception e) {
            // Don't fail the status update if email fails
        }

        return SellerOrderResponse.from(order, seller.getId());
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == to) return false;

        if (from == OrderStatus.PLACED) {
            return to == OrderStatus.CONFIRMED
                    || to == OrderStatus.CANCEL_REQUEST
                    || to == OrderStatus.CANCELLED;
        }
        if (from == OrderStatus.CANCEL_REQUEST) return to == OrderStatus.CANCELLED;
        if (from == OrderStatus.CONFIRMED) return to == OrderStatus.SHIPPED;
        if (from == OrderStatus.SHIPPED) return to == OrderStatus.OUT_FOR_DELIVERY;
        if (from == OrderStatus.OUT_FOR_DELIVERY) return to == OrderStatus.DELIVERED;
        if (from == OrderStatus.DELIVERED) {
            return to == OrderStatus.RETURN_REQUEST
                    || to == OrderStatus.REFUND_REQUEST
                    || to == OrderStatus.REPLACE_REQUEST;
        }
        if (from == OrderStatus.RETURN_REQUEST) return to == OrderStatus.RETURN_PICKUP;
        if (from == OrderStatus.REFUND_REQUEST) return to == OrderStatus.RETURN_PICKUP;
        if (from == OrderStatus.REPLACE_REQUEST) return to == OrderStatus.REPLACE_PICKUP;
        if (from == OrderStatus.RETURN_PICKUP) {
            return to == OrderStatus.RETURNED || to == OrderStatus.REFUNDED;
        }
        if (from == OrderStatus.REPLACE_PICKUP) return to == OrderStatus.REPLACED;
        return false;
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> OrderNotFoundException.id(orderId));
    }

    private Seller findActiveSeller(User user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
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
