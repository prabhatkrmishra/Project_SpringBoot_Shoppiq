package com.pkmprojects.shoppiq.email.repository;

import com.pkmprojects.shoppiq.email.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository
 * ({@link org.springframework.data.jpa.repository.JpaRepository}) for
 * {@link EmailLog} persistence. Demonstrates the <strong>Repository
 * pattern</strong> in a layered architecture.
 *
 * <p><strong>Educational value:</strong> Spring Data JPA repositories
 * provide CRUD operations automatically by extending
 * {@code JpaRepository<T, ID>}. Custom query methods are derived from
 * method names following Spring Data's query derivation conventions:
 * <ul>
 *   <li>{@link #findByUserIdOrderByCreatedAtDesc(Long)} — derives a query
 *       from the method name: "find by userId, order by createdAt descending".
 *       No {@code @Query} annotation needed.</li>
 *   <li>{@link #findByUserIdAndEmailTypeOrderByCreatedAtDesc(Long, EmailType)}
 *       — derives a query with two equality conditions and an order clause.</li>
 *   <li>The {@code @Repository} annotation is optional but recommended for
 *       exception translation (Spring wraps JPA exceptions into
 *       {@link org.springframework.dao.DataAccessException}).</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    /**
     * Finds all email logs for a given user, ordered by creation date descending.
     *
     * @param userId the user ID
     * @return list of email logs
     */
    List<EmailLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds email logs by user ID and email type.
     *
     * @param userId   the user ID
     * @param emailType the email type
     * @return list of matching email logs
     */
    List<EmailLog> findByUserIdAndEmailTypeOrderByCreatedAtDesc(Long userId, com.pkmprojects.shoppiq.email.EmailType emailType);
}
