package com.pkmprojects.shoppiq.aiservice.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Static holder for the Spring {@link ApplicationEventPublisher}.
 *
 * <p>
 * JPA entity listeners are instantiated by Hibernate, not by Spring, so they
 * cannot use {@code @Autowired} fields. This holder is registered as a Spring
 * bean and exposes the publisher statically so entity listeners can publish
 * application events without holding a direct reference to the context.
 *
 * @author PrabhatKrMishra
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
