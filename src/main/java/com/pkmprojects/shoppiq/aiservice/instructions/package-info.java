/**
 * System prompt providers for the AI chat assistant.
 *
 * <p>Contains the strategy interface and implementations for building
 * context-aware system prompts that govern the AI assistant's behavior.
 * Each prompt provider constructs behavioral instructions tailored to
 * the conversation context, including user identity, available tools,
 * formatting rules, and scope restrictions.</p>
 *
 * <p>Separate prompt implementations exist for authenticated users
 * (full feature access including orders, cart, and reviews) and guest
 * users (limited to product catalog search only).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.aiservice.instructions;
