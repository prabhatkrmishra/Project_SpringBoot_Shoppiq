package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Read-only implementation of {@link ChatOrderService}.
 *
 * <p>This service delegates to the {@link OrderRepository} for all order
 * queries, providing a read-only transactional boundary for AI tool data
 * access. The {@code @Transactional(readOnly = true)} annotation ensures
 * that no data modifications occur through this service.</p>
 *
 * <p>The user-order listing method returns up to 50 orders ordered by
 * placement date descending, which is sufficient for the AI assistant's
 * recent order history display.</p>
 *
 * @author prabhatkrmishra
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
        return orderRepository.findAllByUserOrderByPlacedAtDesc(user,
                        PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "placedAt")))
                .getContent();
    }
}
