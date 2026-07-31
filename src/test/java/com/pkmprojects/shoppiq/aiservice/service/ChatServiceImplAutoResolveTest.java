package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.config.ChatMemoryConfig;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.entity.ChatMessage;
import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.aiservice.instructions.SystemPromptProvider;
import com.pkmprojects.shoppiq.aiservice.repository.ChatConversationRepository;
import com.pkmprojects.shoppiq.aiservice.repository.ChatMessageRepository;
import com.pkmprojects.shoppiq.aiservice.tools.ShoppiqTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the auto-resolution heuristic of {@link ChatServiceImpl}.
 *
 * <p>All dependencies are mocked. No Spring context or database is involved.
 * The private {@code shouldAutoResolve(...)} decision method is exercised
 * directly via reflection, matching the repository's existing reflection
 * convention for testing private persistence internals.</p>
 *
 * <p>These tests pin the regression fix for intermittently failing
 * auto-resolution: the decision must inspect the ASSISTANT message that
 * was persisted <em>strictly before</em> the user's closing reply (via the
 * {@code beforeId} boundary), never the assistant reply that is persisted
 * after it.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatServiceImpl auto-resolution tests")
class ChatServiceImplAutoResolveTest {

    private static final long THRESHOLD = 3;

    @Mock
    private ChatMemoryProvider chatMemoryProvider;
    @Mock
    private ChatMemoryConfig chatMemoryConfig;
    @Mock
    private ShoppiqTools shoppiqTools;
    @Mock
    private ContentRetriever contentRetriever;
    @Mock
    private ChatConversationRepository conversationRepository;
    @Mock
    private ChatMessageRepository messageRepository;
    @Mock
    private ModelResolutionService modelResolutionService;
    @Mock
    private SystemPromptProvider authenticatedPrompt;
    @Mock
    private SystemPromptProvider guestPrompt;
    @Mock
    private Clock clock;

    private ChatServiceImpl chatService;

    /**
     * Reflectively sets the private {@code id} field declared in
     * {@code BaseEntity} on any {@code AuditableEntity} subclass.
     * ChatMessage/ChatConversation → AuditableEntity → BaseEntity
     * (id lives 2 levels up).
     */
    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private static ChatMessage message(long id, ChatMessageRole role, String content) throws Exception {
        ChatMessage message = ChatMessage.builder()
                .role(role)
                .content(content)
                .build();
        setId(message, id);
        return message;
    }

    private static ChatConversation conversation(long id) throws Exception {
        ChatConversation conversation = ChatConversation.builder()
                .chatId("CHAT-2026-07-TEST")
                .build();
        setId(conversation, id);
        return conversation;
    }

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(
                chatMemoryProvider,
                chatMemoryConfig,
                shoppiqTools,
                contentRetriever,
                conversationRepository,
                messageRepository,
                modelResolutionService,
                authenticatedPrompt,
                guestPrompt,
                (int) THRESHOLD,
                Clock.fixed(Instant.parse("2026-07-31T10:00:00Z"), ZoneOffset.UTC));
    }

    private boolean shouldAutoResolve(String userMessage, ChatConversation conversation, long userMessageId)
            throws Exception {
        Method method = ChatServiceImpl.class.getDeclaredMethod(
                "shouldAutoResolve", String.class, ChatConversation.class, long.class);
        method.setAccessible(true);
        return (boolean) method.invoke(chatService, userMessage, conversation, userMessageId);
    }

    @Nested
    @DisplayName("userMessageId boundary (regression for intermittent auto-resolve)")
    class MessageIdBoundary {

        @Test
        @DisplayName("queries the assistant message strictly before the user message id")
        void queriesPrecedingAssistantMessageByIdBoundary() throws Exception {
            ChatConversation conv = conversation(10L);
            ChatMessage closingPrompt = message(20L, ChatMessageRole.ASSISTANT,
                    "Is there anything else I can help you with?");
            long userMessageId = 21L;

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER)).thenReturn(5L);
            when(messageRepository.findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                    10L, ChatMessageRole.ASSISTANT, userMessageId))
                    .thenReturn(Optional.of(closingPrompt));

            boolean result = shouldAutoResolve("thanks", conv, userMessageId);

            assertThat(result).isTrue();
            verify(messageRepository).findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                    eq(10L), eq(ChatMessageRole.ASSISTANT), eq(userMessageId));
        }

        @Test
        @DisplayName("does not resolve when no assistant message precedes the user message")
        void noPrecedingAssistantMessage() throws Exception {
            ChatConversation conv = conversation(10L);

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER)).thenReturn(5L);
            when(messageRepository.findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                    10L, ChatMessageRole.ASSISTANT, 21L))
                    .thenReturn(Optional.empty());

            boolean result = shouldAutoResolve("thanks", conv, 21L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("closing prompt matching")
    class ClosingPromptMatching {

        @Test
        @DisplayName("resolves when the preceding assistant message is the standard closing prompt")
        void standardClosingPrompt() throws Exception {
            ChatConversation conv = conversation(10L);
            ChatMessage closingPrompt = message(20L, ChatMessageRole.ASSISTANT,
                    "Is there anything else I can help you with?");

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER)).thenReturn(5L);
            when(messageRepository.findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                    10L, ChatMessageRole.ASSISTANT, 21L))
                    .thenReturn(Optional.of(closingPrompt));

            assertThat(shouldAutoResolve("thanks", conv, 21L)).isTrue();
        }

        @Test
        @DisplayName("resolves for alternate closing prompt wordings")
        void alternateClosingPromptWordings() throws Exception {
            ChatConversation conv = conversation(10L);
            long userMessageId = 21L;

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER)).thenReturn(5L);

            for (String wording : new String[]{
                    "Anything else I can help you with?",
                    "Anything else you need help with?",
                    "Anything else I can do for you?",
                    "Is there anything else you need?",
                    "Sure, is there anything else I can help you with?"}) {
                ChatMessage preceding = message(20L, ChatMessageRole.ASSISTANT, wording);
                when(messageRepository.findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                        10L, ChatMessageRole.ASSISTANT, userMessageId))
                        .thenReturn(Optional.of(preceding));

                assertThat(shouldAutoResolve("that's all", conv, userMessageId))
                        .as("closing wording '%s' should trigger auto-resolve", wording)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("does not resolve when the preceding assistant message is a different question")
        void precedingAssistantMessageIsNotClosingPrompt() throws Exception {
            // Regression guard: "no" answering "Do you want me to filter by size too?"
            // must NOT auto-resolve, even though the user message is a closing phrase.
            ChatConversation conv = conversation(10L);
            ChatMessage filterQuestion = message(20L, ChatMessageRole.ASSISTANT,
                    "Do you want me to filter by size too?");

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER)).thenReturn(5L);
            when(messageRepository.findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                    10L, ChatMessageRole.ASSISTANT, 21L))
                    .thenReturn(Optional.of(filterQuestion));

            assertThat(shouldAutoResolve("no", conv, 21L)).isFalse();
        }

        @Test
        @DisplayName("does not resolve when the preceding assistant message is a friendly closing reply")
        void precedingAssistantMessageIsFriendlyReply() throws Exception {
            // The exact failure mode of the old bug: the assistant's reply to the
            // closing phrase ("You're welcome!") was persisted before the heuristic
            // ran, masking the actual closing prompt. The heuristic must look at the
            // message before the user's closing reply instead.
            ChatConversation conv = conversation(10L);
            ChatMessage friendlyReply = message(20L, ChatMessageRole.ASSISTANT,
                    "You're welcome! Let me know if you need anything else.");

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER)).thenReturn(5L);
            when(messageRepository.findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                    10L, ChatMessageRole.ASSISTANT, 21L))
                    .thenReturn(Optional.of(friendlyReply));

            assertThat(shouldAutoResolve("thanks", conv, 21L)).isFalse();
        }
    }

    @Nested
    @DisplayName("user message matching")
    class UserMessageMatching {

        private ChatMessage closingPrompt() throws Exception {
            return message(20L, ChatMessageRole.ASSISTANT,
                    "Is there anything else I can help you with?");
        }

        @Test
        @DisplayName("resolves for known closing phrases")
        void knownClosingPhrases() throws Exception {
            ChatConversation conv = conversation(10L);
            long userMessageId = 21L;

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER)).thenReturn(5L);
            when(messageRepository.findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                    10L, ChatMessageRole.ASSISTANT, userMessageId))
                    .thenReturn(Optional.of(closingPrompt()));

            for (String phrase : new String[]{
                    "no", "nope", "that's all", "thanks", "thank you",
                    "done", "bye", "goodbye", "all good", "no thanks"}) {
                assertThat(shouldAutoResolve(phrase, conv, userMessageId))
                        .as("closing phrase '%s' should trigger auto-resolve", phrase)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("strips trailing punctuation and ignores case before matching")
        void normalizesInput() throws Exception {
            ChatConversation conv = conversation(10L);
            long userMessageId = 21L;

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER)).thenReturn(5L);
            when(messageRepository.findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                    10L, ChatMessageRole.ASSISTANT, userMessageId))
                    .thenReturn(Optional.of(closingPrompt()));

            assertThat(shouldAutoResolve("Thanks!", conv, userMessageId)).isTrue();
            assertThat(shouldAutoResolve("THANKS", conv, userMessageId)).isTrue();
            assertThat(shouldAutoResolve("  all good.  ", conv, userMessageId)).isTrue();
        }

        @Test
        @DisplayName("does not resolve when the user message is not a closing phrase")
        void notAClosingPhrase() throws Exception {
            ChatConversation conv = conversation(10L);
            long userMessageId = 21L;

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER)).thenReturn(5L);
            when(messageRepository.findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(
                    10L, ChatMessageRole.ASSISTANT, userMessageId))
                    .thenReturn(Optional.of(closingPrompt()));

            assertThat(shouldAutoResolve("show me red sneakers", conv, userMessageId)).isFalse();
            assertThat(shouldAutoResolve("ok", conv, userMessageId)).isFalse();
        }
    }

    @Nested
    @DisplayName("threshold")
    class Threshold {

        @Test
        @DisplayName("does not resolve when the user message count is below the threshold")
        void belowThreshold() throws Exception {
            ChatConversation conv = conversation(10L);
            long userMessageId = 21L;

            when(messageRepository.countByConversationIdAndRole(10L, ChatMessageRole.USER))
                    .thenReturn(THRESHOLD - 1);

            assertThat(shouldAutoResolve("thanks", conv, userMessageId)).isFalse();
        }
    }
}
