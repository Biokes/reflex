package com.eventdriven.eventbus;

/**
 * Functional interface for handling events of a specific type.
 * 
 * @param <T> the type of event this handler processes
 */
@FunctionalInterface
public interface EventHandler<T extends Event> {
    /**
     * Handles the given event.
     * This method is called asynchronously by the EventBus.
     * 
     * @param event the event to handle
     */
    void handle(T event);
}
