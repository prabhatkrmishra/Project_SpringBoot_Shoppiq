package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.auth.ConfirmEmailRequest;
import com.pkmprojects.shoppiq.dto.auth.ForgotPasswordRequest;
import com.pkmprojects.shoppiq.dto.auth.ResetPasswordRequest;
import com.pkmprojects.shoppiq.dto.auth.VerifyEmailRequest;
import com.pkmprojects.shoppiq.email.EmailService;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.VerificationCodeException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.verification.VerificationCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for email verification and password reset flows.
 *
 * @author PrabhatKrMishra
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
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully."
        ));
    }
}
