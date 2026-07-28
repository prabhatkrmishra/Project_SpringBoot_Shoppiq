package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link AdminUserReadModel}
 * providing read-only user queries for admin dashboards and reports.
 *
 * <p>Delegates to {@code UserRepository} for user count and recent-user queries.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional(readOnly = true)</strong> — All queries are read-only, optimized for database performance.</li>
 *   <li><strong>@RequiredArgsConstructor</strong> — Lombok-generated constructor injection for final fields.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see AdminUserReadModel
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AdminUserReadModelImpl implements AdminUserReadModel {

    private final UserRepository userRepository;

    /**
     * Returns the total number of users.
     *
     * @return total user count
     */
    @Override
    public long countAll() {
        return userRepository.count();
    }

    /**
     * Counts users created after the given timestamp.
     *
     * @param instant the reference timestamp
     * @return count of users created after the timestamp
     */
    @Override
    public long countCreatedAfter(Instant instant) {
        return userRepository.countByCreatedAtAfter(instant);
    }

    /**
     * Retrieves the 10 most recently created users.
     *
     * @return list of the 10 most recent users
     */
    @Override
    public List<User> findRecentTop10() {
        return userRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
