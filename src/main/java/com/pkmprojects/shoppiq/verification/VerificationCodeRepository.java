package com.pkmprojects.shoppiq.verification;

import com.pkmprojects.shoppiq.email.EmailType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link VerificationCode} persistence.
 *
 * @author PrabhatKrMishra
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
            Long userId, EmailType emailType, LocalDateTime now);

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
     * Deletes expired codes for cleanup.
     *
     * @param now the current timestamp
     */
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);
}
