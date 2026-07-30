package com.pkmprojects.shoppiq.aiservice.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Static holder for the Spring {@link ApplicationEventPublisher}.
 *
 * <p>JPA entity listeners are instantiated by the JPA provider and are
 * not managed by the Spring application context, which means they cannot
 * directly inject the {@link ApplicationEventPublisher}. This component
 * solves the problem by capturing the publisher reference during
 * construction (when it is injected by Spring) and exposing it via a
 * static method that entity listeners can call.</p>
 *
 * <p>The publisher reference is set once during application startup and
 * remains stable for the lifetime of the application context. The
 * {@link #publish(Object)} method includes a null check to gracefully
 * handle the edge case where an entity event fires before the holder
 * has been initialized.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public class ApplicationEventPublisherHolder {

    private static ApplicationEventPublisher publisher;

    public ApplicationEventPublisherHolder(ApplicationEventPublisher publisher) {
        ApplicationEventPublisherHolder.publisher = publisher;
    }

    public static void publish(Object event) {
        if (publisher != null) {
            publisher.publishEvent(event);
        }
    }
}
