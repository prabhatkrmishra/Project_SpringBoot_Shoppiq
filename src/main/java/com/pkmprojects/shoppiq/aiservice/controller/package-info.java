/**
 * REST controllers for AI chat endpoints.
 *
 * <p>Contains the Spring MVC REST controllers that expose the AI chat
 * API to frontend clients. Controllers are organized by user type:
 * authenticated chat, guest chat, and admin management. Each
 * controller is conditionally enabled via the {@code shoppiq.ai.enabled}
 * property and enforces appropriate security constraints.</p>
 *
 * <p>The authenticated controller supports conversation creation,
 * message sending with optional model selection, conversation history
 * retrieval, and conversation resolution. The guest controller provides
 * a reduced feature set with session-cookie-based tracking.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.aiservice.controller;
