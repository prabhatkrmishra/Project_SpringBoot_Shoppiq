package com.pkmprojects.shoppiq.auth.controller;

import com.pkmprojects.shoppiq.dto.auth.ConfirmEmailRequest;
import com.pkmprojects.shoppiq.dto.auth.ForgotPasswordRequest;
import com.pkmprojects.shoppiq.dto.auth.ResetPasswordRequest;
import com.pkmprojects.shoppiq.dto.auth.VerifyEmailRequest;
import com.pkmprojects.shoppiq.email.EmailService;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.general.verification.VerificationCodeException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import com.pkmprojects.shoppiq.verification.service.VerificationCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for email verification and password reset flows.
 *
 * <h3>Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>Password-less self-service flows</strong> — the controller handles
 *       {@code forgot-password}, {@code reset-password}, {@code verify-email}, and
 *       {@code confirm-email} using time-limited verification codes rather than
 *       requiring the user to be authenticated.</li>
 *   <li><strong>Token version invalidation on password change</strong> — when a
 *       password is reset, the user's {@code tokenVersion} is incremented
 *       ({@code user.setTokenVersion(user.getTokenVersion() + 1)}), which
 *       immediately invalidates all existing JWT sessions. This is a common
 *       pattern for forcing logout after credential changes.</li>
 *   <li><strong>Side-channel verification</strong> — verification codes are sent
 *       via email (out-of-band), proving ownership of the email address without
 *       requiring the user's password.</li>
 * </ul>
 *
 * <h3>Authentication flow</h3>
 * <ol>
 *   <li><b>Forgot password:</b> {@code POST /auth/forgot-password} generates a
 *       6-digit code and emails it to the user (if the email exists — the response
 *       is identical regardless to prevent email enumeration).</li>
 *   <li><b>Reset password:</b> {@code POST /auth/reset-password} validates the code,
 *       updates the password hash via {@code PasswordEncoder}, and increments
 *       the token version to invalidate all existing JWTs.</li>
 *   <li><b>Verify email:</b> {@code POST /auth/verify-email} generates and sends
 *       a verification code.</li>
 *   <li><b>Confirm email:</b> {@code POST /auth/confirm-email} validates the code,
 *       sets {@code emailVerified = true}, and records the verification timestamp.</li>
 * </ol>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Null-object on lookup</strong> — the controller never reveals whether
 *       an email exists in the system; unauthenticated flows silently no-op for
 *       non-existent accounts to prevent email enumeration attacks.</li>
 *   <li><strong>Service delegation</strong> — verification code generation and
 *       validation are handled by {@link com.pkmprojects.shoppiq.verification.service.VerificationCodeService},
 *       keeping the controller focused on HTTP orchestration.</li>
 *   <li><strong>Generic verification codes</strong> — the same {@code VerificationCodeService}
 *       supports both {@code PASSWORD_RESET} and {@code VERIFICATION} email types,
 *       each scoped by an {@link com.pkmprojects.shoppiq.email.EmailType} enum.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class EmailAuthController {

    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Sends a password reset code to the user's email.
     *
     * @param request the forgot password request
     * @return success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        User user = userRepository.findUserByEmail(request.email())
                .orElse(null);

        if (user != null) {
            String code = verificationCodeService.generateCode(user, EmailType.PASSWORD_RESET);

            emailService.sendCriticalEmail(EmailMessage.builder()
                    .to(user.getEmail())
                    .subject("Reset Your Password")
                    .templateName(EmailType.PASSWORD_RESET.getTemplateName())
                    .emailType(EmailType.PASSWORD_RESET)
                    .userId(user.getId())
                    .variables(Map.of(
                            "userName", user.getName(),
                            "verificationCode", code
                    ))
                    .build());
        }

        return ResponseEntity.ok(Map.of(
                "message", "If an account exists with that email, a password reset code has been sent."
        ));
    }

    /**
     * Resets the user's password using a verification code.
     *
     * @param request the reset password request
     * @return success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        User user = userRepository.findUserByEmail(request.email())
                .orElseThrow(() -> new VerificationCodeException(
                        ErrorCode.VERIFICATION_CODE_INVALID, "Invalid email or verification code."));

        verificationCodeService.validateCode(user.getId(), request.code(), EmailType.PASSWORD_RESET);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully."
        ));
    }

    /**
     * Sends an email verification code to the user.
     *
     * @param request the verify email request
     * @return success message
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        User user = userRepository.findUserByEmail(request.email())
                .orElse(null);

        if (user != null) {
            String code = verificationCodeService.generateCode(user, EmailType.VERIFICATION);

            emailService.sendCriticalEmail(EmailMessage.builder()
                    .to(user.getEmail())
                    .subject("Verify Your Email Address")
                    .templateName(EmailType.VERIFICATION.getTemplateName())
                    .emailType(EmailType.VERIFICATION)
                    .userId(user.getId())
                    .variables(Map.of(
                            "userName", user.getName(),
                            "verificationCode", code
                    ))
                    .build());
        }

        return ResponseEntity.ok(Map.of(
                "message", "If an account exists with that email, a verification code has been sent."
        ));
    }

    /**
     * Confirms email verification using a code.
     *
     * @param request the confirm email request
     * @return success message
     */
    @PostMapping("/confirm-email")
    public ResponseEntity<Map<String, String>> confirmEmail(@Valid @RequestBody ConfirmEmailRequest request) {
        User user = userRepository.findUserByEmail(request.email())
                .orElseThrow(() -> new VerificationCodeException(
                        ErrorCode.VERIFICATION_CODE_INVALID, "Invalid email or verification code."));

        verificationCodeService.validateCode(user.getId(), request.code(), EmailType.VERIFICATION);

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully."
        ));
    }
}
