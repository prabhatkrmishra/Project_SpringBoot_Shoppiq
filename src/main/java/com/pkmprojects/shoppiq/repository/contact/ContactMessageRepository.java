package com.pkmprojects.shoppiq.repository.contact;

import com.pkmprojects.shoppiq.entity.contact.ContactMessage;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence operations for the {@link ContactMessage} aggregate.
 *
 * <p>Provides methods to query contact messages with pagination and count by status for admin
 * dashboard statistics. The repository supports ordered queries for message listing and
 * status-based counting for unread message indicators.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    /**
     * Returns all contact messages ordered by creation date descending.
     *
     * @param pageable pagination parameters
     * @return paginated list of contact messages
     */
    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Counts contact messages by status.
     *
     * @param status the message status
     * @return count of messages with the given status
     */
    long countByStatus(ContactMessageStatus status);
}
