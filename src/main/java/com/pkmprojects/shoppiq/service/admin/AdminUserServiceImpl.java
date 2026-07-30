package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminUserResponse;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link AdminUserService} implementation that handles paginated customer retrieval,
 * account blocking/unblocking, and customer dashboard statistics.
 *
 * @author prabhatkrmishra
 * @see AdminUserService
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentLookupService paymentLookupService;
    private final Clock clock;

    public AdminUserServiceImpl(UserRepository userRepository,
                                OrderRepository orderRepository,
                                PaymentLookupService paymentLookupService,
                                Clock clock) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentLookupService = paymentLookupService;
        this.clock = clock;
    }

    /**
     * Retrieves a paginated list of customers with optional enabled/disabled filtering.
     *
     * <p>Results are sorted by creation date descending. Each response is enriched
     * with order count and total spent from the payment system. Aggregates are
     * batch-fetched (BUG-003) to avoid N+1 queries.</p>
     *
     * @param enabled optional filter — {@code true} for active, {@code false} for blocked, {@code null} for all
     * @param page    zero-based page index
     * @param size    page size
     * @return paginated customer responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getAllCustomers(Boolean enabled, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var userPage = (enabled != null)
                ? userRepository.findByEnabled(enabled, pageable)
                : userRepository.findAll(pageable);

        // Batch-fetch aggregates for all users on this page (BUG-003)
        Map<Long, Long> orderCounts = new HashMap<>();
        Map<Long, BigDecimal> totalSpent = new HashMap<>();
        List<User> users = userPage.getContent();

        if (!users.isEmpty()) {
            List<Long> userIds = users.stream().map(User::getId).toList();
            // Order counts
            List<Object[]> countRows = orderRepository.countByUserIds(userIds);
            for (Object[] row : countRows) {
                orderCounts.put((Long) row[0], (Long) row[1]);
            }
            // Payment sums
            totalSpent.putAll(paymentLookupService.sumPaidAmountByUserIds(userIds));
        }

        return PageResponse.of(userPage, user -> {
            Long uid = user.getId();
            return mapToUserResponse(user,
                    orderCounts.getOrDefault(uid, 0L),
                    totalSpent.getOrDefault(uid, BigDecimal.ZERO));
        });
    }

    /**
     * Retrieves a single customer by ID with enriched order/spent data.
     *
     * @param userId customer ID
     * @return customer response with order count and total spent
     * @throws UserNotFoundException if the user does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getCustomerById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.id(userId));
        return mapToUserResponse(user);
    }

    /**
     * Blocks a customer account by setting {@code enabled = false}.
     *
     * <p>The customer will be unable to log in or place orders.</p>
     *
     * @param userId customer ID
     * @return updated customer response
     * @throws UserNotFoundException if the user does not exist
     */
    @Override
    public AdminUserResponse blockCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.id(userId));

        user.setEnabled(false);
        userRepository.save(user);

        return mapToUserResponse(user);
    }

    /**
     * Unblocks a customer account by setting {@code enabled = true}.
     *
     * @param userId customer ID
     * @return updated customer response
     * @throws UserNotFoundException if the user does not exist
     */
    @Override
    public AdminUserResponse unblockCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.id(userId));

        user.setEnabled(true);
        userRepository.save(user);

        return mapToUserResponse(user);
    }

    /**
     * Computes customer dashboard statistics.
     *
     * <p>Returns total, active, and blocked customer counts, new customers
     * this month, total revenue, and average order value.</p>
     *
     * @return customer dashboard stats
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerDashboardStats getCustomerDashboardStats() {
        long totalCustomers = userRepository.count();
        long activeCustomers = userRepository.countByEnabled(true);
        long blockedCustomers = userRepository.countByEnabled(false);

        LocalDate startOfMonth = LocalDate.now(clock).withDayOfMonth(1);
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

    /**
     * Maps a single user to a response DTO, fetching aggregates via individual
     * queries. Used by single-user endpoints (get-by-id, block, unblock) where
     * N+1 is not a concern.
     */
    private AdminUserResponse mapToUserResponse(User user) {
        long totalOrders = orderRepository.countByUser(user);
        BigDecimal totalSpent = Optional.ofNullable(
                        paymentLookupService.sumAmountByUserAndStatus(user, com.pkmprojects.shoppiq.enums.PaymentStatus.PAID))
                .orElse(BigDecimal.ZERO);

        return AdminUserResponse.fromEntity(user, totalOrders, totalSpent);
    }

    /**
     * Maps a user to a response DTO using pre-computed aggregates.
     * Used by paginated listing methods to avoid N+1 (BUG-003).
     */
    private AdminUserResponse mapToUserResponse(User user, long totalOrders, BigDecimal totalSpent) {
        return AdminUserResponse.fromEntity(user, totalOrders, totalSpent);
    }
}
