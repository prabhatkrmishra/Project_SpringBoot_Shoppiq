package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link NewsletterSubscriber} persistence.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);

    Optional<NewsletterSubscriber> findByToken(String token);

    boolean existsByEmailIgnoreCase(String email);

    List<NewsletterSubscriber> findAllByActiveTrue();

    @Query("SELECT ns.email FROM NewsletterSubscriber ns WHERE ns.active = true")
    List<String> findAllActiveEmails();
}
