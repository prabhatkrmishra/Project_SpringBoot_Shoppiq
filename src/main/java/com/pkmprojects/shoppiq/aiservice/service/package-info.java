/**
 * Service layer for AI chat operations.
 *
 * <p>Contains the core service interfaces and implementations that
 * orchestrate AI chat conversations. This includes the primary
 * {@code ChatService} for message processing, conversation lifecycle
 * management, and auto-resolution; read-only facades for product,
 * order, and review data access; model resolution for multimodel
 * support; and admin services for conversation monitoring.</p>
 *
 * <p>The service layer bridges the LangChain4j AI proxy interfaces
 * with Shoppiq's persistence and business logic, handling memory
 * management, tool invocation, and streaming response assembly.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.aiservice.service;
