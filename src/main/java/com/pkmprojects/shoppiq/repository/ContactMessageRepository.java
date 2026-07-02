package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link ContactMessage} persistence operations.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
