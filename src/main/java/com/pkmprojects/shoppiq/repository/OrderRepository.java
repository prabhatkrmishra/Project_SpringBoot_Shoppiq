package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Order} persistence.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserOrderByPlacedAtDesc(User user);

    Page<Order> findAll(Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Optional<Order> findById(Long id);

    List<Order> findTop10ByOrderByPlacedAtDesc();

    long countByStatus(OrderStatus status);

    long countByPlacedAtBetween(Instant start, Instant end);

    List<Order> findByPlacedAtBetweenOrderByPlacedAtAsc(Instant start, Instant end);

    List<Order> findByPlacedAtBetween(Instant start, Instant end);

    long countByUser(User user);
}
