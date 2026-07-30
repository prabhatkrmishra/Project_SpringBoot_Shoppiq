package com.pkmprojects.shoppiq.aiservice.instructions;

import com.pkmprojects.shoppiq.entity.user.User;

/**
 * Provides a system prompt for an AI chat conversation.
 *
 * <p>This strategy interface defines the contract for building context-aware
 * system prompts that govern the AI assistant's behavior during conversations.
 * Implementations supply behavioral instructions that shape the model's
 * responses, including available tools, formatting rules, scope restrictions,
 * and user identity information.</p>
 *
 * <p>The system prompt is injected into the LangChain4j AI service proxy at
 * conversation time via the {@code systemMessageProvider} configuration.
 * Different implementations exist for authenticated users (full feature
 * access) and guest users (limited product search only).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface SystemPromptProvider {

    /**
     * Builds the system prompt for a conversation.
     *
     * <p>The returned prompt text is injected as the system message for the AI
     * model and governs its behavior throughout the conversation. The prompt
     * includes the user's identity (if authenticated), available tools,
     * formatting rules, and scope restrictions.</p>
     *
     * @param chatId the conversation's public identifier
     * @param user   the user (may be {@code null} for guest conversations)
     * @return the system prompt text
     */
    String buildPrompt(String chatId, User user);
}
