package com.pkmprojects.shoppiq.repository.newsletter;

import com.pkmprojects.shoppiq.entity.newsletter.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link NewsletterSubscriber} persistence.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Case-insensitive lookup</strong> — {@code findByEmailIgnoreCase} generates
 *       {@code SELECT * FROM newsletter_subscribers WHERE LOWER(email) = LOWER(?)}.</li>
 *   <li><strong>Derived exists query</strong> — {@code existsByEmailIgnoreCase} checks
 *       existence without fetching the full entity.</li>
 *   <li><strong>Boolean field filtering</strong> — {@code findAllByActiveTrue} generates
 *       {@code SELECT * FROM newsletter_subscribers WHERE active = TRUE}.</li>
 *   <li><strong>JPQL scalar projection</strong> — {@code findAllActiveEmails} uses
 *       {@code SELECT ns.email FROM ...} to return only the email column (a
 *       {@code List<String>}), avoiding full entity loading for bulk email campaigns.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findByEmailIgnoreCase(String)
 *       → SELECT * FROM newsletter_subscribers WHERE LOWER(email) = LOWER(?)
 *   findByToken(String)
 *       → SELECT * FROM newsletter_subscribers WHERE token = ?
 *   existsByEmailIgnoreCase(String)
 *       → SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM ... WHERE LOWER(email) = LOWER(?)
 *   findAllByActiveTrue()
 *       → SELECT * FROM newsletter_subscribers WHERE active = TRUE
 * </pre>
 *
 * @author prabhatkrmishra
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
