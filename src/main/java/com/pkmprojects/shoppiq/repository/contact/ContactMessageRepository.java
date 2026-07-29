package com.pkmprojects.shoppiq.repository.contact;

import com.pkmprojects.shoppiq.entity.contact.ContactMessage;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link ContactMessage} persistence operations.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived count queries</strong> — {@code countByStatus} generates
 *       {@code SELECT COUNT(*) FROM contact_messages WHERE status = ?}.</li>
 *   <li><strong>Pagination with ordering</strong> — {@code findAllByOrderByCreatedAtDesc}
 *       accepts an optional {@link org.springframework.data.domain.Pageable} parameter;
 *       when a {@code Pageable} is supplied, Spring Data automatically wraps the query
 *       with a count query and applies {@code LIMIT} / {@code OFFSET}.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findAllByOrderByCreatedAtDesc
 *       → SELECT * FROM contact_messages ORDER BY created_at DESC
 *   countByStatus(ContactMessageStatus)
 *       → SELECT COUNT(*) FROM contact_messages WHERE status = ?
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(ContactMessageStatus status);
}
