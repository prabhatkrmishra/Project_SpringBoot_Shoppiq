/**
 * JPA entities for AI chat persistence.
 *
 * <p>Contains the JPA entity classes that model AI chat conversations
 * and their associated messages. These entities are persisted to the
 * relational database and provide the backing store for conversation
 * lifecycle management, message history, and admin auditing.</p>
 *
 * <p>The entity model supports both authenticated user conversations
 * (linked to a {@code User} entity) and guest session conversations
 * (tracked by session UUID and IP address).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.aiservice.entity;
