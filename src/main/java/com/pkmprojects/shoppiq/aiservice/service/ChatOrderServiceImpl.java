package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Read-only implementation of {@link ChatOrderService}.
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ChatOrderServiceImpl implements ChatOrderService {

    private final OrderRepository orderRepository;

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> findByUserNewestFirst(User user) {
        return orderRepository.findAllByUserOrderByPlacedAtDesc(user);
    }
}
