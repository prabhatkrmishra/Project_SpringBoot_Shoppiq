package com.pkmprojects.shoppiq.email.repository;

import com.pkmprojects.shoppiq.email.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link EmailLog} persistence.
 *
 * @author PrabhatKrMishra
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
