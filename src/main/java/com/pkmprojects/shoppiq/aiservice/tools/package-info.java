/**
 * LangChain4j tool definitions for the AI assistant.
 *
 * <p>Contains the tool method implementations that give the AI model
 * access to Shoppiq's data and operations. Tools are invoked by the
 * LLM during conversations to retrieve product details, check order
 * status, view cart contents, access user reviews, perform semantic
 * product search, and resolve conversations.</p>
 *
 * <p>Each tool method is annotated with {@code @Tool} and includes
 * descriptive text that the LLM uses to decide when to invoke it.
 * Tools are only available to authenticated users; guest conversations
 * rely solely on RAG-based product retrieval.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.aiservice.tools;
