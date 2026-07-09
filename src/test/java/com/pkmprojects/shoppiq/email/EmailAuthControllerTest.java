package com.pkmprojects.shoppiq.email;

import com.pkmprojects.shoppiq.controller.EmailAuthController;
import com.pkmprojects.shoppiq.dto.auth.ConfirmEmailRequest;
import com.pkmprojects.shoppiq.dto.auth.ForgotPasswordRequest;
import com.pkmprojects.shoppiq.dto.auth.ResetPasswordRequest;
import com.pkmprojects.shoppiq.dto.auth.VerifyEmailRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.VerificationCodeException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.verification.VerificationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailAuthController")
class EmailAuthControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmailAuthController controller;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
        setId(testUser, 1L);
    }

    private void setId(Object entity, Long id) throws Exception {
        Field f = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }

    @Nested
    @DisplayName("forgotPassword")
    class ForgotPasswordTests {

        @Test
        @DisplayName("should return success message even if user not found")
        void shouldReturnSuccessMessage() {
            when(userRepository.findUserByEmail(any())).thenReturn(Optional.empty());

            ResponseEntity<Map<String, String>> response = controller.forgotPassword(
                    new ForgotPasswordRequest("test@example.com"));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).containsKey("message");
        }

        @Test
        @DisplayName("should send email when user exists")
        void shouldSendEmailWhenUserExists() {
            when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(verificationCodeService.generateCode(any(), any())).thenReturn("123456");

            ResponseEntity<Map<String, String>> response = controller.forgotPassword(
                    new ForgotPasswordRequest("test@example.com"));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(emailService).sendCriticalEmail(any());
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("should reset password with valid code")
        void shouldResetPassword() {
            when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(verificationCodeService.validateCode(1L, "123456", EmailType.PASSWORD_RESET)).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

            ResponseEntity<Map<String, String>> response = controller.resetPassword(
                    new ResetPasswordRequest("test@example.com", "123456", "newPassword123"));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).containsEntry("message", "Password has been reset successfully.");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should throw exception for invalid code")
        void shouldThrowForInvalidCode() {
            when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(verificationCodeService.validateCode(1L, "000000", EmailType.PASSWORD_RESET))
                    .thenThrow(new VerificationCodeException(ErrorCode.VERIFICATION_CODE_INVALID, "Invalid code."));

            assertThatThrownBy(() -> controller.resetPassword(
                    new ResetPasswordRequest("test@example.com", "000000", "newPassword123")))
                    .isInstanceOf(VerificationCodeException.class);
        }
    }

    @Nested
    @DisplayName("verifyEmail")
    class VerifyEmailTests {

        @Test
        @DisplayName("should return success message")
        void shouldReturnSuccessMessage() {
            when(userRepository.findUserByEmail(any())).thenReturn(Optional.empty());

            ResponseEntity<Map<String, String>> response = controller.verifyEmail(
                    new VerifyEmailRequest("test@example.com"));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).containsKey("message");
        }

        @Test
        @DisplayName("should send verification email when user exists")
        void shouldSendVerificationEmail() {
            when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(verificationCodeService.generateCode(any(), any())).thenReturn("123456");

            ResponseEntity<Map<String, String>> response = controller.verifyEmail(
                    new VerifyEmailRequest("test@example.com"));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(emailService).sendCriticalEmail(any());
        }
    }

    @Nested
    @DisplayName("confirmEmail")
    class ConfirmEmailTests {

        @Test
        @DisplayName("should confirm email with valid code")
        void shouldConfirmEmail() {
            when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(verificationCodeService.validateCode(1L, "123456", EmailType.VERIFICATION)).thenReturn(true);

            ResponseEntity<Map<String, String>> response = controller.confirmEmail(
                    new ConfirmEmailRequest("test@example.com", "123456"));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).containsEntry("message", "Email verified successfully.");
        }

        @Test
        @DisplayName("should throw exception for invalid code")
        void shouldThrowForInvalidCode() {
            when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(verificationCodeService.validateCode(1L, "000000", EmailType.VERIFICATION))
                    .thenThrow(new VerificationCodeException(ErrorCode.VERIFICATION_CODE_INVALID, "Invalid code."));

            assertThatThrownBy(() -> controller.confirmEmail(
                    new ConfirmEmailRequest("test@example.com", "000000")))
                    .isInstanceOf(VerificationCodeException.class);
        }
    }
}
