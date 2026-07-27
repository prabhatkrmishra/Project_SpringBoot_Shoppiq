package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.general.user.UserNotFoundException;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import com.pkmprojects.shoppiq.service.payment.PaymentLookupService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Default implementation of {@link AdminUserService}.
 *
 * <p>
 * Provides customer management operations for administrators
 * including retrieval, blocking, and unblocking accounts.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Retrieve paginated customers with optional enabled filter.</li>
 *     <li>Retrieve single customer by ID.</li>
 *     <li>Block/unblock customer accounts.</li>
 *     <li>Provide customer dashboard statistics.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>Read operations use read-only transactions.</li>
 *     <li>Blocking disables login by setting {@code enabled=false}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentLookupService paymentLookupService;

    public AdminUserServiceImpl(UserRepository userRepository,
                                OrderRepository orderRepository,
                                PaymentLookupService paymentLookupService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentLookupService = paymentLookupService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getAllCustomers(Boolean enabled, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var userPage = (enabled != null)
                ? userRepository.findByEnabled(enabled, pageable)
                : userRepository.findAll(pageable);

        return PageResponse.of(userPage, this::mapToUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getCustomerById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.id(userId));
        return mapToUserResponse(user);
    }

    @Override
    public AdminUserResponse blockCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.id(userId));

        user.setEnabled(false);
        userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Override
    public AdminUserResponse unblockCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.id(userId));

        user.setEnabled(true);
        userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDashboardStats getCustomerDashboardStats() {
        long totalCustomers = userRepository.count();
        long activeCustomers = userRepository.countByEnabled(true);
        long blockedCustomers = userRepository.countByEnabled(false);

        LocalDate startOfMonth = LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1);
        Instant startInstant = startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        long newCustomersThisMonth = userRepository.countByCreatedAtAfter(startInstant);

        BigDecimal totalRevenue = Optional.ofNullable(
                        paymentLookupService.sumAmountByStatus(com.pkmprojects.shoppiq.enums.PaymentStatus.PAID))
                .orElse(BigDecimal.ZERO);

        BigDecimal averageOrderValue = orderRepository.count() > 0
                ? totalRevenue.divide(BigDecimal.valueOf(orderRepository.count()), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new CustomerDashboardStats(
                totalCustomers,
                activeCustomers,
                blockedCustomers,
                newCustomersThisMonth,
                totalRevenue,
                averageOrderValue
        );
    }

    private AdminUserResponse mapToUserResponse(User user) {
        long totalOrders = orderRepository.countByUser(user);
        BigDecimal totalSpent = Optional.ofNullable(
                        paymentLookupService.sumAmountByUserAndStatus(user, com.pkmprojects.shoppiq.enums.PaymentStatus.PAID))
                .orElse(BigDecimal.ZERO);

        return AdminUserResponse.fromEntity(user, totalOrders, totalSpent);
    }
}
