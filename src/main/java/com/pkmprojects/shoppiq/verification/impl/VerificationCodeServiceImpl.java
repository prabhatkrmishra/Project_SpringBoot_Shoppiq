package com.pkmprojects.shoppiq.verification.impl;

import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.VerificationCodeException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.verification.VerificationCode;
import com.pkmprojects.shoppiq.verification.VerificationCodeRepository;
import com.pkmprojects.shoppiq.verification.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Default implementation of {@link VerificationCodeService}.
 *
 * @author PrabhatKrMishra
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

    @Override
    @Transactional
    public String generateCode(User user, EmailType emailType) {
        verificationCodeRepository.markAllUnusedCodesAsUsed(user.getId(), emailType);

        String code = generateNumericCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(VerificationCode.CODE_VALIDITY_MINUTES);

        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .emailType(emailType)
                .expiresAt(expiresAt)
                .build();

        verificationCodeRepository.save(verificationCode);
        log.info("Verification code generated for user={}, type={}", user.getId(), emailType);

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

        if (LocalDateTime.now().isAfter(verificationCode.getExpiresAt())) {
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

        verificationCode.markUsed();
        verificationCodeRepository.save(verificationCode);
        log.info("Verification code validated successfully for user={}, type={}", userId, emailType);

        return true;
    }

    private String generateNumericCode() {
        int code = SECURE_RANDOM.nextInt(CODE_UPPER_BOUND);
        return String.format("%0" + CODE_LENGTH + "d", code);
    }
}
