/**
 * Configuration classes for the AI chat service.
 *
 * <p>This package contains Spring {@code @Configuration} classes that
 * define and wire all AI-related beans for the Shoppiq assistant.
 * Configurations include per-conversation chat memory management,
 * Retrieval-Augmented Generation (RAG) with Qdrant vector store
 * integration, NVIDIA NIM-backed chat model initialization, and the
 * top-level ChatService assembly.</p>
 *
 * <p>All configurations are conditionally enabled via the
 * {@code shoppiq.ai.enabled} property, allowing the AI module to
 * be completely disabled in non-AI environments.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.aiservice.config;
