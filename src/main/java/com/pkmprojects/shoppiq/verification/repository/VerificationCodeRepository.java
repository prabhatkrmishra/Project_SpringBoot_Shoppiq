package com.pkmprojects.shoppiq.verification.repository;

import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.verification.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Repository for {@link VerificationCode} persistence.
 *
 * <p><b>How it fits:</b> Provides atomic mark-used operations and
 * batch invalidation of stale codes. The {@code findTopByUserIdAndEmailTypeAndUsedFalseAndExpiresAtAfter}
 * derived query efficiently retrieves the latest valid (unused,
 * non-expired) code for a user.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * Finds the latest unused and non-expired code for a user and email type.
     *
     * @param userId    the user ID
     * @param emailType the email type
     * @return the verification code if found
     */
    Optional<VerificationCode> findTopByUserIdAndEmailTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId, EmailType emailType, Instant now);

    /**
     * Finds a specific code for a user and email type.
     *
     * @param userId    the user ID
     * @param code      the verification code
     * @param emailType the email type
     * @return the verification code if found
     */
    Optional<VerificationCode> findByUserIdAndCodeAndEmailType(Long userId, String code, EmailType emailType);

    /**
     * Marks all unused codes for a user and email type as used.
     *
     * @param userId    the user ID
     * @param emailType the email type
     */
    @Modifying
    @Query("UPDATE VerificationCode v SET v.used = true WHERE v.user.id = :userId AND v.emailType = :emailType AND v.used = false")
    void markAllUnusedCodesAsUsed(@Param("userId") Long userId, @Param("emailType") EmailType emailType);

    /**
     * Atomically marks a specific code as used if it has not been used already.
     *
     * @param id the verification code ID
     * @return the number of rows updated (0 if already used, 1 if successfully marked)
     */
    @Modifying
    @Query("UPDATE VerificationCode v SET v.used = true WHERE v.id = :id AND v.used = false")
    int markUsedAtomically(@Param("id") Long id);

    /**
     * Deletes expired codes for cleanup.
     *
     * @param now the current timestamp
     */
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") Instant now);
}
