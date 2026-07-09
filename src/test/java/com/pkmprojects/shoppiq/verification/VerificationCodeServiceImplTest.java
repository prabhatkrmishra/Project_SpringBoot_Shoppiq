package com.pkmprojects.shoppiq.verification;

import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.VerificationCodeException;
import com.pkmprojects.shoppiq.verification.impl.VerificationCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationCodeServiceImpl")
class VerificationCodeServiceImplTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @InjectMocks
    private VerificationCodeServiceImpl verificationCodeService;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
        setId(testUser, 1L);
    }

    private void setId(Object entity, Long id) throws Exception {
        Field f = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }

    @Nested
    @DisplayName("generateCode")
    class GenerateCodeTests {

        @Test
        @DisplayName("should generate a 6-digit code")
        void shouldGenerateSixDigitCode() {
            when(verificationCodeRepository.save(any(VerificationCode.class)))
                    .thenReturn(new VerificationCode());

            String code = verificationCodeService.generateCode(testUser, EmailType.VERIFICATION);

            assertThat(code).hasSize(6);
            assertThat(code).matches("\\d{6}");
            verify(verificationCodeRepository).markAllUnusedCodesAsUsed(1L, EmailType.VERIFICATION);
            verify(verificationCodeRepository).save(any(VerificationCode.class));
        }

        @Test
        @DisplayName("should invalidate previous codes before generating new one")
        void shouldInvalidatePreviousCodes() {
            when(verificationCodeRepository.save(any(VerificationCode.class)))
                    .thenReturn(new VerificationCode());

            verificationCodeService.generateCode(testUser, EmailType.PASSWORD_RESET);

            verify(verificationCodeRepository).markAllUnusedCodesAsUsed(1L, EmailType.PASSWORD_RESET);
        }
    }

    @Nested
    @DisplayName("validateCode")
    class ValidateCodeTests {

        @Test
        @DisplayName("should validate correct code")
        void shouldValidateCorrectCode() throws Exception {
            VerificationCode validCode = VerificationCode.builder()
                    .user(testUser)
                    .code("123456")
                    .emailType(EmailType.VERIFICATION)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .used(false)
                    .attempts(0)
                    .build();
            setId(validCode, 1L);

            when(verificationCodeRepository.findByUserIdAndCodeAndEmailType(1L, "123456", EmailType.VERIFICATION))
                    .thenReturn(Optional.of(validCode));
            when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);

            boolean result = verificationCodeService.validateCode(1L, "123456", EmailType.VERIFICATION);

            assertThat(result).isTrue();
            assertThat(validCode.isUsed()).isTrue();
        }

        @Test
        @DisplayName("should reject invalid code")
        void shouldRejectInvalidCode() {
            when(verificationCodeRepository.findByUserIdAndCodeAndEmailType(1L, "000000", EmailType.VERIFICATION))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    verificationCodeService.validateCode(1L, "000000", EmailType.VERIFICATION))
                    .isInstanceOf(VerificationCodeException.class);
        }

        @Test
        @DisplayName("should reject expired code")
        void shouldRejectExpiredCode() throws Exception {
            VerificationCode expiredCode = VerificationCode.builder()
                    .user(testUser)
                    .code("123456")
                    .emailType(EmailType.VERIFICATION)
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .used(false)
                    .attempts(0)
                    .build();
            setId(expiredCode, 1L);

            when(verificationCodeRepository.findByUserIdAndCodeAndEmailType(1L, "123456", EmailType.VERIFICATION))
                    .thenReturn(Optional.of(expiredCode));

            assertThatThrownBy(() ->
                    verificationCodeService.validateCode(1L, "123456", EmailType.VERIFICATION))
                    .isInstanceOf(VerificationCodeException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("should reject used code")
        void shouldRejectUsedCode() throws Exception {
            VerificationCode usedCode = VerificationCode.builder()
                    .user(testUser)
                    .code("123456")
                    .emailType(EmailType.VERIFICATION)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .used(true)
                    .attempts(0)
                    .build();
            setId(usedCode, 1L);

            when(verificationCodeRepository.findByUserIdAndCodeAndEmailType(1L, "123456", EmailType.VERIFICATION))
                    .thenReturn(Optional.of(usedCode));

            assertThatThrownBy(() ->
                    verificationCodeService.validateCode(1L, "123456", EmailType.VERIFICATION))
                    .isInstanceOf(VerificationCodeException.class)
                    .hasMessageContaining("already been used");
        }

        @Test
        @DisplayName("should lock code after max attempts")
        void shouldLockCodeAfterMaxAttempts() throws Exception {
            VerificationCode code = VerificationCode.builder()
                    .user(testUser)
                    .code("123456")
                    .emailType(EmailType.PASSWORD_RESET)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .used(false)
                    .attempts(2)
                    .build();
            setId(code, 1L);

            when(verificationCodeRepository.findByUserIdAndCodeAndEmailType(1L, "123456", EmailType.PASSWORD_RESET))
                    .thenReturn(Optional.of(code));
            when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(code);

            assertThatThrownBy(() ->
                    verificationCodeService.validateCode(1L, "123456", EmailType.PASSWORD_RESET))
                    .isInstanceOf(VerificationCodeException.class)
                    .hasMessageContaining("Maximum");

            assertThat(code.isUsed()).isTrue();
        }
    }
}
