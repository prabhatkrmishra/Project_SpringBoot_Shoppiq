package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.repository.order.OrderItemRepository;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Default implementation of {@link AdminOrderReadModel}.
 *
 * <p>Delegates to {@code OrderRepository} and {@code OrderItemRepository}
 * for aggregate order queries used in admin dashboards and reports.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AdminOrderReadModelImpl implements AdminOrderReadModel {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public long countAll() {
        return orderRepository.count();
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public long countPlacedBetween(Instant start, Instant end) {
        return orderRepository.countByPlacedAtBetween(start, end);
    }

    @Override
    public List<Order> findPlacedBetweenAsc(Instant start, Instant end) {
        return orderRepository.findByPlacedAtBetweenOrderByPlacedAtAsc(start, end);
    }

    @Override
    public List<Order> findRecentTop10() {
        return orderRepository.findTop10ByOrderByPlacedAtDesc();
    }

    @Override
    public List<Object[]> aggregateRevenueBySeller(PaymentStatus paymentStatus) {
        return orderItemRepository.aggregateRevenueBySeller(paymentStatus);
    }
}
