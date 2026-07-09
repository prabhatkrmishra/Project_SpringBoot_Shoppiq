package com.pkmprojects.shoppiq.email;

import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.email.entity.EmailLog;
import com.pkmprojects.shoppiq.email.impl.EmailServiceImpl;
import com.pkmprojects.shoppiq.email.provider.EmailProvider;
import com.pkmprojects.shoppiq.email.provider.EmailProviderRegistry;
import com.pkmprojects.shoppiq.email.repository.EmailLogRepository;
import com.pkmprojects.shoppiq.entity.NotificationPreference;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.EmailSendException;
import com.pkmprojects.shoppiq.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl")
class EmailServiceImplTest {

    @Mock
    private EmailProviderRegistry providerRegistry;

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private EmailProvider emailProvider;

    @InjectMocks
    private EmailServiceImpl emailService;

    private EmailMessage testMessage;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
        setId(testUser, 1L);

        testMessage = EmailMessage.builder()
                .to("recipient@example.com")
                .subject("Test Subject")
                .templateName("verification")
                .emailType(EmailType.VERIFICATION)
                .userId(1L)
                .build();
    }

    private void setId(Object entity, Long id) throws Exception {
        Field f = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }

    @Nested
    @DisplayName("sendEmail")
    class SendEmailTests {

        @Test
        @DisplayName("should send email when user preference allows")
        void shouldSendEmailWhenPreferenceAllows() {
            when(providerRegistry.getActiveProvider()).thenReturn(emailProvider);
            when(emailProvider.getProviderName()).thenReturn("CONSOLE");
            when(preferenceRepository.findByUserId(1L)).thenReturn(
                    Optional.of(NotificationPreference.builder()
                            .user(testUser)
                            .accountSecurity(true)
                            .build())
            );
            when(emailLogRepository.save(any(EmailLog.class))).thenReturn(new EmailLog());

            emailService.sendEmail(testMessage);

            verify(emailProvider).send(testMessage);
            verify(emailLogRepository).save(any(EmailLog.class));
        }

        @Test
        @DisplayName("should skip email when user preference disables it")
        void shouldSkipEmailWhenPreferenceDisables() {
            when(preferenceRepository.findByUserId(1L)).thenReturn(
                    Optional.of(NotificationPreference.builder()
                            .user(testUser)
                            .accountSecurity(false)
                            .build())
            );
            when(emailLogRepository.save(any(EmailLog.class))).thenReturn(new EmailLog());

            emailService.sendEmail(testMessage);

            verify(emailProvider, never()).send(any());
            verify(emailLogRepository).save(any(EmailLog.class));
        }

        @Test
        @DisplayName("should send email when no preference exists")
        void shouldSendEmailWhenNoPreferenceExists() {
            when(providerRegistry.getActiveProvider()).thenReturn(emailProvider);
            when(emailProvider.getProviderName()).thenReturn("CONSOLE");
            when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(emailLogRepository.save(any(EmailLog.class))).thenReturn(new EmailLog());

            emailService.sendEmail(testMessage);

            verify(emailProvider).send(testMessage);
        }

        @Test
        @DisplayName("should handle email send failure gracefully")
        void shouldHandleSendFailureGracefully() {
            when(providerRegistry.getActiveProvider()).thenReturn(emailProvider);
            when(emailProvider.getProviderName()).thenReturn("CONSOLE");
            when(preferenceRepository.findByUserId(1L)).thenReturn(
                    Optional.of(NotificationPreference.builder()
                            .user(testUser)
                            .accountSecurity(true)
                            .build())
            );
            doThrow(new EmailSendException("SMTP error"))
                    .when(emailProvider).send(any());
            when(emailLogRepository.save(any(EmailLog.class))).thenReturn(new EmailLog());

            emailService.sendEmail(testMessage);

            ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);
            verify(emailLogRepository).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getStatus()).isEqualTo(EmailLog.EmailStatus.FAILED);
            assertThat(logCaptor.getValue().getErrorMessage()).contains("SMTP error");
        }
    }

    @Nested
    @DisplayName("sendCriticalEmail")
    class SendCriticalEmailTests {

        @Test
        @DisplayName("should send critical email regardless of preferences")
        void shouldSendCriticalEmailRegardlessOfPreferences() {
            when(providerRegistry.getActiveProvider()).thenReturn(emailProvider);
            when(emailProvider.getProviderName()).thenReturn("CONSOLE");
            when(emailLogRepository.save(any(EmailLog.class))).thenReturn(new EmailLog());

            emailService.sendCriticalEmail(testMessage);

            verify(emailProvider).send(testMessage);
            verify(preferenceRepository, never()).findByUserId(any());
        }
    }
}
