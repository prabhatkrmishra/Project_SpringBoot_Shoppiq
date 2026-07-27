package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Default implementation of {@link AdminUserReadModel}.
 *
 * <p>Delegates to {@code UserRepository} for user aggregate queries
 * used in admin dashboards and reports.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AdminUserReadModelImpl implements AdminUserReadModel {

    private final UserRepository userRepository;

    @Override
    public long countAll() {
        return userRepository.count();
    }

    @Override
    public long countCreatedAfter(Instant instant) {
        return userRepository.countByCreatedAtAfter(instant);
    }

    @Override
    public List<User> findRecentTop10() {
        return userRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
