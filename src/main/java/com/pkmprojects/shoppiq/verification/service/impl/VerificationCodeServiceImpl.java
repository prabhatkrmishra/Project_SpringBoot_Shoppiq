package com.pkmprojects.shoppiq.verification.service.impl;

import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.general.verification.VerificationCodeException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.verification.entity.VerificationCode;
import com.pkmprojects.shoppiq.verification.repository.VerificationCodeRepository;
import com.pkmprojects.shoppiq.verification.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Default implementation of {@link VerificationCodeService}.
 *
 * <p><b>How it fits:</b> Generates cryptographically random 6-digit
 * codes for email verification and password reset. Uses an atomic
 * mark-used query to prevent race conditions during verification.
 * Existing unused codes are invalidated when a new code is generated
 * for the same user and type.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final int CODE_UPPER_BOUND = (int) Math.pow(10, CODE_LENGTH);

    private final VerificationCodeRepository verificationCodeRepository;
    private final Clock clock;

    @Override
    @Transactional
    public String generateCode(User user, EmailType emailType) {
        verificationCodeRepository.markAllUnusedCodesAsUsed(user.getId(), emailType);

        String code = generateNumericCode();
        Instant expiresAt = Instant.now(clock).plus(Duration.ofMinutes(VerificationCode.CODE_VALIDITY_MINUTES));

        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .emailType(emailType)
                .expiresAt(expiresAt)
                .build();

        verificationCodeRepository.save(verificationCode);
        log.debug("Verification code generated for user={}, type={}", user.getId(), emailType);

        return code;
    }

    @Override
    @Transactional
    public boolean validateCode(Long userId, String code, EmailType emailType) {
        VerificationCode verificationCode = verificationCodeRepository
                .findByUserIdAndCodeAndEmailType(userId, code, emailType)
                .orElseThrow(() -> new VerificationCodeException(
                        ErrorCode.VERIFICATION_CODE_INVALID, "Invalid verification code."));

        if (verificationCode.isUsed()) {
            throw new VerificationCodeException(
                    ErrorCode.VERIFICATION_CODE_INVALID, "Verification code has already been used.");
        }

        if (Instant.now(clock).isAfter(verificationCode.getExpiresAt())) {
            throw new VerificationCodeException(
                    ErrorCode.VERIFICATION_CODE_EXPIRED, "Verification code has expired.");
        }

        verificationCode.incrementAttempts();

        if (verificationCode.getAttempts() >= VerificationCode.MAX_ATTEMPTS) {
            verificationCode.markUsed();
            verificationCodeRepository.save(verificationCode);
            throw new VerificationCodeException(
                    ErrorCode.VERIFICATION_CODE_MAX_ATTEMPTS, "Maximum verification attempts exceeded.");
        }

        int updated = verificationCodeRepository.markUsedAtomically(verificationCode.getId());
        if (updated == 0) {
            throw new VerificationCodeException(
                    ErrorCode.VERIFICATION_CODE_INVALID, "Verification code has already been used.");
        }

        log.debug("Verification code validated successfully for user={}, type={}", userId, emailType);

        return true;
    }

    /**
     * Generates a cryptographically random numeric code of fixed length.
     *
     * @return a zero-padded numeric string
     */
    private String generateNumericCode() {
        int code = SECURE_RANDOM.nextInt(CODE_UPPER_BOUND);
        return ("%0" + CODE_LENGTH + "d").formatted(code);
    }
}
